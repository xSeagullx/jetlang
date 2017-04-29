package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

public class ReduceExpression extends Expression {
	private final Expression sequenceExpr;
	private final Expression initialValueExpr;
	private final LambdaExpression lambda;

	public ReduceExpression(Expression sequenceExpr, Expression initialValueExpr, LambdaExpression lambda) {
		this.sequenceExpr = sequenceExpr;
		this.initialValueExpr = initialValueExpr;
		this.lambda = lambda;
	}

	@Override public Object exec(ExecutionContext context) {
		Object maybeSequence = context.exec(sequenceExpr);
		if (!(maybeSequence instanceof Sequence))
			throw context.exception("First argument to reduce shall be a sequence: Found: " + maybeSequence, this);

		Sequence sequence = (Sequence)maybeSequence;
		Object initialValue = context.exec(initialValueExpr);
		return context.reduce(sequence, initialValue, lambda);
	}
}
