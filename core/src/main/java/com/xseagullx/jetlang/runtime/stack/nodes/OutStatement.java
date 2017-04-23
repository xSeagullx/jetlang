package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class OutStatement extends Statement {
	private final Expression expression;

	public OutStatement(Expression expression) {
		this.expression = expression;
	}

	@Override public void exec(ExecutionContext context) {
		context.print(context.exec(expression));
	}
}
