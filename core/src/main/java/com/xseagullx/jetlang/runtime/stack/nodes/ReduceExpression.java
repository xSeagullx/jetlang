package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

public class ReduceExpression implements Expression {
	private final Expression sequenceExpr;
	private final Expression initialValueExpr;
	private final LambdaExpression lambda;

	public ReduceExpression(Expression sequenceExpr, Expression initialValueExpr, LambdaExpression lambda) {
		this.sequenceExpr = sequenceExpr;
		this.initialValueExpr = initialValueExpr;
		this.lambda = lambda;
	}

	@Override public Object exec(ExecutionContext context) {
		Object maybeSequence = sequenceExpr.exec(context);
		if (!(maybeSequence instanceof Sequence))
			throw new RuntimeException("First argument to map shall be a sequence: Found: " + maybeSequence);

		Sequence sequence = (Sequence)maybeSequence;

		Object initialValue = initialValueExpr.exec(context);

		return sequence.list.stream().reduce(initialValue, (acc, i) -> lambda.apply(context, acc, i));
	}
}
