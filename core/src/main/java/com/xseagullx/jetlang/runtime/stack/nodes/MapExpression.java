package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;
import com.xseagullx.jetlang.Sequence;

import java.util.stream.Collectors;

public class MapExpression implements Expression {
	private final Expression sequenceExpr;
	private final LambdaExpression lambda;

	public MapExpression(Expression sequenceExpr, LambdaExpression lambda) {
		this.sequenceExpr = sequenceExpr;
		this.lambda = lambda;
	}

	@Override public Object exec(ExecutionContext context) {
		Object maybeSequence = sequenceExpr.exec(context);
		if (!(maybeSequence instanceof Sequence))
			throw new JetLangException("First argument to map shall be a sequence: Found: " + maybeSequence);

		Sequence sequence = (Sequence)maybeSequence;
		return new Sequence(sequence.list.stream().map( i -> lambda.apply(context, i)).collect(Collectors.toList()));
	}
}
