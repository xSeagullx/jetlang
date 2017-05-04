package com.xseagullx.jetlang.runtime.jvm

import com.xseagullx.jetlang.ExecutionContext
import com.xseagullx.jetlang.Sequence
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification
import com.xseagullx.jetlang.Compiler
import spock.lang.Unroll

import java.lang.reflect.Modifier

class ASMTest extends Specification {
	@Rule TestName name = new TestName()

	def "test asm"() {
		setup:
		def executionContext = Mock(ExecutionContext)
		def programCtx = Compiler.getJetLangParser(Compiler.getJetLangLexer("var a = 512 + 4.2\nout a", []), []).program()

		when:
		def bc = new JavaBytecodeCompiler().generateClass(programCtx)
		new File("Program.class").bytes = bc
		Class clazz = loadClass(bc)

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

	@Unroll("Check that #expression == #result")
	def "operation test"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + expression, executionContext)

		then:
		executionContext.print(_) >> { obj ->
			assert obj[0].getClass() == resultType
			assert obj[0] == result
		}

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
		"-1.2 * -7.84"      || -1.2d * -7.84d | Double
		"2 * 3 + 1"         || 7            | Integer
		"2 * 3 + 1 / 4.0"   || 6.25         | Double
		"2 * (3 + 1) / 4"   || 2            | Integer
		"+1 + +2"           || 3            | Integer
	}

	def "ranges"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + "{1, 3}", executionContext)

		then:
		1 * executionContext.print(_) >> { args ->
			assert args[0] == new Sequence(1, 3)
		}
	}

	def "maps"() {
		setup:
		def executionContext = Mock(ExecutionContext)

		when:
		exec("out " + "map({1, 3}, i -> i * 2)", executionContext)

		then:
		1 * executionContext.print(_) >> { args ->
			assert args[0] == new Sequence([2, 4, 6])
		}
	}

	Class<?> loadClass(byte[] bytes) {
		Class<?> clazz = null
		// Class will keep a reference to classloader
		//noinspection GroovyResultOfObjectAllocationIgnored
		new URLClassLoader() {
			{
				clazz = defineClass(null, bytes, 0, bytes.size())
			}
		}
		clazz
	}

	private void exec(String text, ExecutionContext executionContext) {
		def programCtx = Compiler.getJetLangParser(Compiler.getJetLangLexer(text, []), []).program()
		def classFileBytes = new JavaBytecodeCompiler().generateClass(programCtx)
		def name = name.methodName
			.replaceAll("\\+", "plus")
			.replaceAll("\\-", "minus")
			.replaceAll("\\/", "div")
			.replaceAll("\\*", "mul")
			.replaceAll("\\^", "pow")
			.replaceAll("==", "equals")
			.replaceAll("\\.", "_")
		new File(name + ".class").bytes = classFileBytes

		Class clazz = loadClass(classFileBytes)
		def program = (ProgramBase) clazz.newInstance()
		program.context = executionContext
		program.run()
	}
}
