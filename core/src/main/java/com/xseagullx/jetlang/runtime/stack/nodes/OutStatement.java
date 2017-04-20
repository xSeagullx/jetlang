package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class OutStatement implements Statement {
	private final Expression expression;

	public OutStatement(Expression expression) {
		this.expression = expression;
	}

	@Override public void exec(ExecutionContext context) {
		context.print(expression.exec(context));
	}
}
