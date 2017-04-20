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

	@Unroll("Check that #expression == #result")
	def "operators"() {
		when:
		execute("var a = " + expression)

		then: "state is preserved"
		context.getVariable("a") == result

		where:
		expression          || result
		"1 + 2"             || 3
		"42 - 12"           || 30
		"4 * 2"             || 8
		"14 / 2"            || 7
		"2 ^ 8"             || 256
		"1.2 + 4.7"         || 5.9
		"1.2 - 4.7"         || -3.5
		"-1.2 - 4.7"        || -5.9
		"-1.2 + 4.7"        || 3.5
		"-1.2 * -7.8"       || -1.2 * -7.8
		"2 * 3 + 1"         || 7
		"2 * 3 + 1 / 4"     || 6.25
		"2 * (3 + 1) / 4"   || 2
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
		def expectedMap = (1..5).toList().collect { (((-1.0d) ** it as double) / (2.0 * it + 1)) as double }
		def expectedPi = 4 * expectedMap.inject(0) { acc, it -> acc + it }

		def text = """
		var n = 5
		var sequence = map({1, n}, i -> (-1)^i / (2 * i + 1))
		var pi = 4 * reduce(sequence, 0, x y -> x + y)
		print "pi = "
		out pi
		"""

		when:
		execute(text)

		then: "values are computed correctly"
		context.getVariable("n") == 5
		(context.getVariable("sequence") as Sequence).list == expectedMap
		context.getVariable("pi") == expectedPi

		and: "there was an output"
		1 * context.print("pi = ")
		1 * context.print(expectedPi)
	}

	private execute(String program) {
		new StackMachineCompiler().parse(program).execute(context)
	}
}
