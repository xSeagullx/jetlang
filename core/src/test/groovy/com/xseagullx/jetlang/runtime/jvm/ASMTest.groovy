package com.xseagullx.jetlang.runtime.jvm

import com.xseagullx.jetlang.ExecutionContext
import com.xseagullx.jetlang.Sequence
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Modifier

class ASMTest extends Specification {
	@Rule TestName name = new TestName()
	String classFilesDir = System.getProperty("classFilesDir")

	def "asm"() {
		setup:
		def executionContext = Mock(ExecutionContext)
		def text = "var a = 512 + 4.2\nout a"

		when:
		def jvmProgram = new JavaBytecodeCompiler().parse(text).program as JvmProgram
		dumpClassFile(jvmProgram.bytes)
		Class clazz = jvmProgram.loadClass()

		then: "class is generated"
		clazz.superclass == ProgramBase
		clazz.name == "Program"
		Modifier.isPublic(clazz.modifiers)

		when: "we can run it"
		def program = (ProgramBase) clazz.newInstance()
		program.context = executionContext
		program.run()

		then:
		1 * executionContext.print(516.2)
	}

	@SuppressWarnings("GroovyAssignabilityCheck")
	@Unroll("Check that #expression == #result")
	def "operation test"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + expression, executionContext)

		then:
		1 * executionContext.print(_) >> { args ->
			assert (args as Object[])[0].getClass() == resultType
			assert (args as Object[])[0] == result
		}

		where:
		expression          || result       | resultType
		"+2"                || 2            | Integer
		"-2"                || -2           | Integer
		"-2.2"              || -2.2         | Double
		"+2.2"              || 2.2          | Double
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
		"-1.2 * -7.84"      || -1.2 * -7.84 | Double
		"2 * 3 + 1"         || 7            | Integer
		"2 * 3 + 1 / 4.0"   || 6.25         | Double
		"2 * (3 + 1) / 4"   || 2            | Integer
		"+1 + +2"           || 3            | Integer
		"1+--+2"            || 3            | Integer
		"1+--+2.1"          || 3.1          | Double
	}

	@SuppressWarnings("GroovyAssignabilityCheck")
	@Unroll("Check that variable can be negated. #expression produces #result")
	def "unary operations on variables test"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec(expression, executionContext)

		then:
		1 * executionContext.print(_) >> { args ->
			assert (args as Object[])[0].getClass() == resultType
			assert (args as Object[])[0] == result
		}

		where:
		expression            || result       | resultType
		"var a = 5\nout -a"   || -5           | Integer
		"var a = 5.3\nout -a" || -5.3         | Double
		"var a = 5\nout +a"   || 5            | Integer
		"var a = 5.3\nout +a" || 5.3          | Double
	}

	def "ranges"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + "{1, 3}", executionContext)

		then:
		//noinspection GroovyAssignabilityCheck
		1 * executionContext.print(_) >> { args ->
			assert (args as Object[])[0] == new Sequence(1, 3)
		}
	}

	def "map"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + "map({1, 3}, i -> i * 2)", executionContext)

		then:
		//noinspection GroovyAssignabilityCheck
		1 * executionContext.print(_) >> { args ->
			assert (args as Object[])[0] == new Sequence([2, 4, 6])
		}
	}

	def "reduce"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + "reduce({1, 3}, 10, i s -> s + i)", executionContext)

		then:
		//noinspection GroovyAssignabilityCheck
		1 * executionContext.print(_) >> { args ->
			assert (args as Object[])[0] == 10 + 1 + 2 + 3
		}
	}

	def "pi"() {
		setup:
		def executionContext = Mock(ExecutionContext)
		def text = """
		var n = 150
		var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))
		var pi = 4 * reduce(sequence, 0, x y -> x + y)
		print "pi = "
		out pi
		"""
		def out = []

		when:
		exec(text, executionContext)

		then:
		//noinspection GroovyAssignabilityCheck
		2 * executionContext.print(_) >> { def args ->
			out << (args as Object[])[0]
		}

		and:
		out == ["pi = ", 3.1482150975379377]
	}

	private void exec(String text, ExecutionContext executionContext) {
		def jvmProgram = new JavaBytecodeCompiler().parse(text).program
		def classFileBytes = (jvmProgram as JvmProgram).bytes
		dumpClassFile(classFileBytes)
		jvmProgram.execute(executionContext)
	}

	private void dumpClassFile(byte[] bytes) {
		if (!classFilesDir)
			return

		def baseDir = new File(classFilesDir)
		baseDir.mkdirs()

		if (baseDir.exists() && baseDir.isDirectory()) {
			def name = name.methodName
				.replaceAll("\\+", "plus")
				.replaceAll("-", "minus")
				.replaceAll("/", "div")
				.replaceAll("\\*", "mul")
				.replaceAll("\\^", "pow")
				.replaceAll("==", "equals")
				.replaceAll("\\.", "_")
			new File(baseDir, name + ".class").bytes = bytes
		}
	}
}
