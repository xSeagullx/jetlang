package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.runtime.CompilationVisitor;

public class StackMachineCompiler extends Compiler {
	@Override protected CompilationVisitor getVisitor() {
		return new StackMachineVisitor();
	}
}
