package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

import java.util.List;

public class LambdaExpression {
	private final List<String> variableNames;
	private final Expression body;

	public LambdaExpression(List<String> variableNames, Expression body) {
		this.variableNames = variableNames;
		this.body = body;
	}

	Object apply(ExecutionContext context, Object ... args) {
		context.push();
		defineArgumentVariables(context, args);
		Object res = body.exec(context);
		context.pop();
		return res;
	}

	private void defineArgumentVariables(ExecutionContext context, Object[] args) {
		for (int i = 0; i < args.length; i++)
			context.defineVariable(variableNames.get(i), args[i]);
	}
}
