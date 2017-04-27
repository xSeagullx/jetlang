package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

public class MapExpression extends Expression {
	private final Expression sequenceExpr;
	private final LambdaExpression lambda;

	public MapExpression(Expression sequenceExpr, LambdaExpression lambda) {
		this.sequenceExpr = sequenceExpr;
		this.lambda = lambda;
	}

	@Override public Object exec(ExecutionContext context) {
		Object maybeSequence = context.exec(sequenceExpr);
		if (!(maybeSequence instanceof Sequence))
			throw context.exception("First argument to map shall be a sequence: Found: " + maybeSequence, this);

		Sequence sequence = (Sequence)maybeSequence;
		return new Sequence(context.map(sequence.list, lambda));
	}
}
