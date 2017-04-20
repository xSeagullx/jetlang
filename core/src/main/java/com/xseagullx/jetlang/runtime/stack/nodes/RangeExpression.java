package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

public class RangeExpression implements Expression {
	private final Expression from;
	private final Expression to;

	public RangeExpression(Expression from, Expression to) {
		this.from = from;
		this.to = to;
	}

	@Override public Object exec(ExecutionContext context) {
		Object fromVal = from.exec(context);
		Object toVal = to.exec(context);
		if (!(fromVal instanceof Integer) || !(toVal instanceof Integer))
			throw new RuntimeException("Cannot create range {" + fromVal +", " + toVal + "}");
		Integer from = (Integer)fromVal;
		Integer to = (Integer)toVal;
		if (from > to)
			throw new RuntimeException("Range is inverse {" + from +", " + to + "}");

		return new Sequence(from, to);
	}
}
