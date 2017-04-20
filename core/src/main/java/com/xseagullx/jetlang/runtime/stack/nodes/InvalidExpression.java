package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class InvalidExpression implements Expression {
	private final String message;

	public InvalidExpression(String message) {
		this.message = message;
	}

	@Override public Object exec(ExecutionContext context) {
		context.error("Expression is not implemented: " + message);
		throw new UnsupportedOperationException();
	}
}
