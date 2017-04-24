package com.xseagullx.jetlang

import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler
import spock.lang.Specification
import spock.lang.Unroll

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
		"var sequence = map({, n}, i -> 1 + 3)"             || "[20..21] line 1:20 extraneous input ',' expecting {INTEGER, REAL_NUMBER, '{', '(', 'reduce', 'map', IDENTIFIER}"
		"var 5 = 10"                                        || "[4..5] line 1:4 mismatched input '5' expecting IDENTIFIER"
	}
}
