package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.JetLangCompiler;
import com.xseagullx.jetlang.runtime.CompilationVisitor;

public class StackMachineCompiler extends JetLangCompiler {
	@Override protected CompilationVisitor getVisitor() {
		return new StackMachineVisitor();
	}
}
