package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.TokenInformationHolder;

import java.util.List;

public class LambdaExpression extends TokenInformationHolder {
	private final List<String> variableNames;
	private final Expression body;

	public LambdaExpression(List<String> variableNames, Expression body) {
		this.variableNames = variableNames;
		this.body = body;
	}

	Object apply(ExecutionContext context, Object ... args) {
		context.push(this);
		defineArgumentVariables(context, args);
		Object res = context.exec(body);
		context.pop();
		return res;
	}

	private void defineArgumentVariables(ExecutionContext context, Object[] args) {
		for (int i = 0; i < args.length; i++)
			context.defineVariable(variableNames.get(i), args[i]);
	}
}
