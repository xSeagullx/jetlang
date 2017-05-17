package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.JetLangCompiler;
import com.xseagullx.jetlang.runtime.CompilationVisitor;

public class JavaBytecodeCompiler extends JetLangCompiler {
	@Override protected CompilationVisitor getVisitor() {
		return new JVMCompilationContext();
	}
}
