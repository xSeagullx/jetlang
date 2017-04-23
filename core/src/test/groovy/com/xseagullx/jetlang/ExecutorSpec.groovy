package com.xseagullx.jetlang

import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext
import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler
import spock.lang.Specification
import spock.lang.Unroll

class ExecutorSpec extends Specification {
	def context = Spy(SimpleExecutionContext)

	def "define and print variable"() {
		when:
		def program = """
			var a = 5
			print "a ="
			out a
		"""

		execute(program)

		then:"Program produces output"
		1 * context.print("a =")
		1 * context.print(5)

		and: "state is preserved"
		context.isVariableDefined("a")
		context.getVariable("a") == 5
	}

	def "work with range"() {
		when:
		execute("var a = {1, 5}")

		then: "state is preserved"
		context.isVariableDefined("a")
		def variable = context.getVariable("a")
		variable instanceof Sequence
		def sequence = variable as Sequence
		sequence.list == [1, 2, 3, 4, 5]
	}

	@SuppressWarnings("GroovyAssignabilityCheck")
	@Unroll("Check that #expression == #result")
	def "operators"() {
		when:
		execute("var a = " + expression)

		then: "state is preserved"
		def aValue = context.getVariable("a")
		aValue == result
		aValue.getClass() == resultType

		where:
		expression          || result       | resultType
		"1 + 2"             || 3            | Integer
		"42 - 12"           || 30           | Integer
		"4 * 2"             || 8            | Integer
		"14 / 3"            || 4            | Integer
		"2 ^ 8"             || 256          | Double
		"25 ^ 0.5"          || 5            | Double
		"1.2 + 4.7"         || 5.9          | Double
		"1.2 - 4.7"         || -3.5         | Double
		"-1.2 - 4.7"        || -5.9         | Double
		"-1.2 + 4.7"        || 3.5          | Double
		"-1.2 * -7.8"       || -1.2 * -7.8  | Double
		"2 * 3 + 1"         || 7            | Integer
		"2 * 3 + 1 / 4.0"   || 6.25         | Double
		"2 * (3 + 1) / 4"   || 2            | Integer
	}

	@Unroll("Map #expr to #result")
	def "map function"() {
		when:
		execute("var a = $expr")

		then:
		def variable = context.getVariable("a")
		variable instanceof Sequence
		def sequence = variable as Sequence
		sequence.list == result

		where:
		expr                                                | comment           || result
		"map({1, 5}, i -> i + 1)"                           | "int -> int"      || [2, 3, 4, 5, 6]
		"map({1, 3}, i -> i * 0.5)"                         | "int -> double"   || [0.5, 1, 1.5]
		"map({1, 1}, i -> i * 0.5)"                         | "single item"     || [0.5]
		"map(map({1, 3}, i -> i + 0.5), i -> i + 2)"        | "nested"          || [3.5, 4.5, 5.5]
	}

	def "execute example program"() {
		setup: "Calc pi same way, as in program"
		def expectedMap = (0..150).toList().collect { (((-1.0d) ** it as double) / (2.0 * it + 1)) as double }
		def expectedPi = 4 * expectedMap.inject(0) { acc, it -> acc + it }
		println(expectedPi)

		def text = """
		var n = 150
		var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))
		var pi = 4 * reduce(sequence, 0, x y -> x + y)
		print "pi = "
		out pi
		"""

		when:
		execute(text)

		then: "values are computed correctly"
		context.getVariable("n") == 150
		(context.getVariable("sequence") as Sequence).list == expectedMap
		context.getVariable("pi") == expectedPi

		and: "there was an output"
		1 * context.print("pi = ")
		1 * context.print(expectedPi)
	}

	@Unroll("#text produces error: '#errorMessage'")
	def "error handling"() {
		when:
		execute(text)

		then:
		def e = thrown(JetLangException)
		e.message == errorMessage

		where:
		text || errorMessage
		"var a = {1.2, 3}"                  || "Cannot create range form non-integer bounds {1.2, 3}"
		"var a = {1, 3.2}"                  || "Cannot create range form non-integer bounds {1, 3.2}"
		"var a = {1, {2, 3}}"               || "Cannot create range form non-integer bounds {1, [2, 3]}"
		"var a = {{1, 3}, 2}"               || "Cannot create range form non-integer bounds {[1, 2, 3], 2}"
		"var a = {3, 2}"                    || "Cannot create range. Bounds are inverse {3, 2}"
		"var a = 5 + {1, 3}"                || "binary op: PLUS cannot be applied to [5, [1, 2, 3]]"
		"var a = 5.0 + {1, 3}"              || "binary op: PLUS cannot be applied to [5.0, [1, 2, 3]]"
		"var a = {1, 3} + {6, 8}"           || "binary op: PLUS cannot be applied to [[1, 2, 3], [6, 7, 8]]" // it would be cool to have a concatenation here
		"var a = map(1, i -> i)"            || "First argument to map shall be a sequence: Found: 1"
		"var a = reduce(1, 1, i a -> i)"    || "First argument to reduce shall be a sequence: Found: 1"
	}

	private execute(String program) {
		new StackMachineCompiler().parse(program).execute(context)
	}
}
