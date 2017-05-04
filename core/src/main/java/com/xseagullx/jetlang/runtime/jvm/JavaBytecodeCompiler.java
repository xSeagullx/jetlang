package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.runtime.CompilationVisitor;

public class JavaBytecodeCompiler extends Compiler {
	@Override protected CompilationVisitor getVisitor() {
		return new JVMCompilationContext();
	}
}
