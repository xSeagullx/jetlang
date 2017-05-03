package com.xseagullx.jetlang.runtime.jvm

import com.xseagullx.jetlang.ExecutionContext
import spock.lang.Specification
import com.xseagullx.jetlang.Compiler

import java.lang.reflect.Modifier

class ASMTest extends Specification {
	def "test asm"() {
		setup:
		def executionContext = Mock(ExecutionContext)
		def programCtx = Compiler.getJetLangParser(Compiler.getJetLangLexer("var a = 512\nout a", []), []).program()

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
		1 * executionContext.print(512)
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
}
