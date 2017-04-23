package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class PrintStatement extends Statement {
	private final String string;

	public PrintStatement(String string) {
		this.string = string;
	}

	@Override public void exec(ExecutionContext context) {
		context.print(string);
	}
}
