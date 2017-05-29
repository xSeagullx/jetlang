package com.xseagullx.jetlang.runtime.stack

import com.xseagullx.jetlang.JetLangException
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletionException

class ParserSpec extends Specification {
	@Unroll("#text produces error: #error")
	def "parser errors"() {
		when: "there is a text with errors"
		def program = new StackMachineCompiler().parse(text)
		def firstError = program.errors.first()

		then: "Check only first error. Normal programmers shall not care about subsequent errors in same statement, until first one is fixed."
		"[" + firstError.startOffset + ".." + firstError.endOffset + "] " + firstError.toString() == error

		where:
		text                                                || error
		"var sequence = map({, n}, i -> 1 + 3)"             || "[20..21] line 1:21 extraneous input ',' expecting {INTEGER, REAL_NUMBER, '+', '-', '{', '(', 'reduce', 'map', IDENTIFIER}"
		"var 5 = 10"                                        || "[4..5] line 1:5 mismatched input '5' expecting IDENTIFIER"
		"var a = 100000000000"                              || "[8..20] line 1:9 NumberFormatException"
	}

	@Unroll("Program: #text produces error: #error and stacktrace #stacktrace")
	def "test runtime errors position"() {
		setup:
		def context = new SimpleExecutionContext(new ForkJoinExecutor(100))

		when:
		new StackMachineCompiler().parse(text).program.execute(context)

		then:
		def e = thrown(RuntimeException)
		JetLangException exception = unwrap(e)

		and:
		exception.message == error
		exception.getJetLangStackTrace().collect { it.toString() } == stacktrace

		where:
		text                                                       || error                                                   | stacktrace
		"var a = map({1, 3}, i -> {i, 2})"                         || "Cannot create range. Bounds are inverse {3, 2}"        | ["LambdaExpression 1:21", "main 1:1"]
		"var a = map({1, 30}, i -> map({i, i + 3}, i2 -> i2 / 0))" || "/ by zero"                                             | ["LambdaExpression 1:43", "LambdaExpression 1:22", "main 1:1"]
	}

	JetLangException unwrap(RuntimeException e) {
		(e instanceof CompletionException ? e.cause : e) as JetLangException
	}
}
