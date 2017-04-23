package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class VariableDeclaration implements Statement {
	private final String name;
	private final Expression expression;

	public VariableDeclaration(String name, Expression expression) {
		this.name = name;
		this.expression = expression;
	}

	@Override public void exec(ExecutionContext context) {
		if (context.isVariableDefined(name))
			throw new RuntimeException("Variable " + name + " is already defined");

		context.defineVariable(name, context.exec(expression));
	}
}

