package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class VariableExpression extends Expression {
	private final String name;

	public VariableExpression(String name) {
		this.name = name;
	}

	@Override public Object exec(ExecutionContext context) {
		return context.getVariable(name);
	}
}
