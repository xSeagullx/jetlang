package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

public class UnaryExpression extends Expression {
	private final Expression expr;
	private final OperationType operationType;

	public UnaryExpression(Expression expr, OperationType operationType) {
		this.expr = expr;
		this.operationType = operationType;
	}

	@Override public Object exec(ExecutionContext context) {
		Object maybeNumber = context.exec(expr);
		if (!(maybeNumber instanceof Number))
			throw context.exception("unary op: " + operationType + " cannot be applied to " + maybeNumber, this);

		if (operationType == OperationType.PLUS)
			return maybeNumber; // unary plus is a no-op

		if (maybeNumber instanceof Double)
			return -((Double)maybeNumber);
		else if (maybeNumber instanceof Integer)
			return -((Integer)maybeNumber);

		throw new ThisShouldNeverHappenException("Unsupported Number type: " + maybeNumber);
	}
}
