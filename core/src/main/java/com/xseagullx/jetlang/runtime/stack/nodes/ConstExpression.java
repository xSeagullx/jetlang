package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public class ConstExpression<T> implements Expression {
	private final T value;

	public ConstExpression(T value) {
		this.value = value;
	}

	@Override public T exec(ExecutionContext context) {
		return value;
	}
}
