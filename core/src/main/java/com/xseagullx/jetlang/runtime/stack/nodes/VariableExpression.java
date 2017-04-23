package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;

public class VariableExpression extends Expression {
	private final String name;

	public VariableExpression(String name) {
		this.name = name;
	}

	@Override public Object exec(ExecutionContext context) {
		if (!context.isVariableDefined(name))
			throw new JetLangException("Undeclared identifier: '" + name + "'", this);
		return context.getVariable(name);
	}
}
