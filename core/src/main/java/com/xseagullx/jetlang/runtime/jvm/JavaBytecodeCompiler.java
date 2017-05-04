package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.ParseError;

import java.util.ArrayList;
import java.util.List;

public class JavaBytecodeCompiler extends Compiler {
	@Override protected CompilationResult doParse(JetLangParser.ProgramContext program) {
		List<ParseError> errors = new ArrayList<>();
		JVMCompilationContext jvmCompilationContext = new JVMCompilationContext();
		jvmCompilationContext.errors = errors;
		jvmCompilationContext.visit(program);

		if (!errors.isEmpty())
			return new CompilationResult(errors);

		byte[] bytes = jvmCompilationContext.classWriter.toByteArray();
		return new CompilationResult(new JvmProgram(bytes));
	}
}
