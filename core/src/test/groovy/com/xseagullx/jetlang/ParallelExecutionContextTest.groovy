package com.xseagullx.jetlang

import com.xseagullx.jetlang.runtime.stack.ForkJoinExecutor
import com.xseagullx.jetlang.runtime.stack.ParallelExecutor
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext
import com.xseagullx.jetlang.runtime.stack.nodes.ConstExpression
import com.xseagullx.jetlang.runtime.stack.nodes.OutStatement
import spock.lang.Specification

import java.util.concurrent.CompletionException

class ParallelExecutionContextTest extends Specification {
	ParallelExecutor forkJoinExecutor = Stub(ForkJoinExecutor, constructorArgs: [100])
	def context = new SimpleExecutionContext(forkJoinExecutor)

	def "when we branch context, interruption flag is preserved in child"() {
		setup:
		def parallelContext = context.copy()

		when:
		parallelContext.stopExecution(null, null)

		then:
		context.getExecutionOutcome().completedExceptionally
	}

	def "when we branch context, interruption flag is preserved in parent"() {
		setup:
		def parallelContext = context.copy()

		when:
		context.stopExecution(null, null)

		then:
		parallelContext.getExecutionOutcome().completedExceptionally
	}

	def "if context is stopped, expression execution does nothing"() {
		setup:
		context.stopExecution(null, null)
		Object res

		when:
		res = context.exec(new ConstExpression<Integer>(5))

		then:
		def e = thrown(CompletionException)
		e.cause instanceof JetLangException
		res == null
	}

	def "if context is stopped, statement execution does nothing"() {
		setup:
		def mockStatement = Mock(OutStatement)
		context.stopExecution(null, null)

		when:
		context.exec(mockStatement)

		then:
		def e = thrown(CompletionException)
		e.cause instanceof JetLangException
		0 * mockStatement.exec(context)
	}
}
