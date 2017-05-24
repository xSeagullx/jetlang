package com.xseagullx.jetlang

import com.xseagullx.jetlang.runtime.stack.ForkJoinExecutor
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression
import com.xseagullx.jetlang.runtime.stack.nodes.ConstExpression
import com.xseagullx.jetlang.runtime.stack.nodes.Expression
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression
import com.xseagullx.jetlang.runtime.stack.nodes.MapExpression
import com.xseagullx.jetlang.runtime.stack.nodes.OperationType
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger

class ParallelMapSpec extends Specification {
	def executor = new ForkJoinExecutor(100)
	def report = new ConcurrentHashMap<String, AtomicInteger>().withDefault { new AtomicInteger(0) }

	def lambdaMock = Mock(LambdaExpression, constructorArgs: [["i"], Mock(Expression)]) {
		apply(_, _) >> { ExecutionContext _, Object[] args ->
			def threadName = Thread.currentThread().name
			report.get(threadName).incrementAndGet()
			return args[0] * 2
		}
	}

	def "map over a small collection is executed sequentially"() {
		setup:
		def sequence = getSequence(100)
		def context = new SimpleExecutionContext(executor)

		when:
		def result = executor.map(context, sequence, lambdaMock)

		then:
		report.size() == 1
		report.keySet().first() == Thread.currentThread().name
		printReport(report)

		and: "results are correct"
		result == expectedMap(sequence)
	}

	def "map is executed in parallel"() {
		setup:
		def sequence = getSequence(1000)
		def context = Spy(SimpleExecutionContext, constructorArgs: [executor])

		when:
		def result = executor.map(context, sequence, lambdaMock)

		then:
		report.size() > 1 // tasks were executed in separate threads
		printReport(report)

		and: "Context has been copied"
		16 * context.copy()

		and: "results are correct"
		result.collect { it } == expectedMap(sequence)

		where:
		i << (1..10)
	}

	def "map can handle exceptions"() {
		setup:
		def context = new SimpleExecutionContext(executor)

		def sequence = (-5000..5000).toList()
		def lambdaMockWithRandomException = Mock(LambdaExpression, constructorArgs: [["i"], Mock(Expression)]) {
			apply(_, _) >> { ExecutionContext ctx, Object[] args ->
				return ctx.exec(new BinaryExpression(new ConstExpression(args[0]), new ConstExpression(args[0]), OperationType.DIV))
			}
		}

		def expression = new MapExpression(Mock(Expression) {
			exec(_) >> new Sequence(sequence)
		}, lambdaMockWithRandomException)

		when:
		def result = context.exec(expression)

		then:
		thrown(JetLangException)

		context.getExecutionOutcome().completedExceptionally
		result == null
	}

	def "map can handle user cancellation"() {
		setup:
		def context = new SimpleExecutionContext(executor)

		def sequence = getSequence(10000)
		def lambdaMockWithRandomException = Mock(LambdaExpression, constructorArgs: [["i"], Mock(Expression)]) {
			apply(_, _) >> { ExecutionContext ctx, Object[] args ->
				Thread.sleep(10)
				println("Processing map element: " + args[0])
				return ctx.exec(new ConstExpression(args[0]))
			}
		}

		def expression = new MapExpression(Mock(Expression) {
			exec(_) >> new Sequence(sequence)
		}, lambdaMockWithRandomException)

		when:
		def result = CompletableFuture.runAsync{ context.exec(expression) } // start Worker Thread
		sleep(100)
		context.stopExecution(null)
		result.get()

		then:
		def e = thrown(ExecutionException)
		e.cause instanceof JetLangException

		and:
		context.getExecutionOutcome().completedExceptionally
	}

	void cleanup() {
		executor.destroy()
	}

	private static List<Integer> getSequence(int size) {
		(1..size).toList()
	}

	private static void printReport(Map<String, AtomicInteger> report) {
		println("Workload distribution: ")
		println(report.collect{ "$it.key : ${it.value.get()}" }.join("\n"))
	}

	private static List<Integer> expectedMap(List<Integer> sequence) {
		sequence.collect { it * 2 }
	}
}
