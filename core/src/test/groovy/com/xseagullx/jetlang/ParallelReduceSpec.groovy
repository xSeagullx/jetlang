package com.xseagullx.jetlang

import com.xseagullx.jetlang.runtime.stack.ForkJoinExecutor
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression.OperationType
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression
import com.xseagullx.jetlang.runtime.stack.nodes.VariableExpression
import spock.lang.Specification

class ParallelReduceSpec extends Specification {
	def "parallel reduce works"() {
		setup:
		def sequence = (1..100).toList()
		def executor = new ForkJoinExecutor(10)
		def context = new SimpleExecutionContext(executor)

		def expression = new BinaryExpression(new VariableExpression("a"), new VariableExpression("b"), OperationType.PLUS)

		when:
		def result = executor.reduce(context, sequence, 13, new LambdaExpression(["a", "b"], expression))

		then:
		result == sequence.inject(13) { acc, it -> acc + it }

		cleanup:
		executor.destroy()
	}
}
