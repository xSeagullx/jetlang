package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

public class RangeExpression extends Expression {
	private final Expression from;
	private final Expression to;

	public RangeExpression(Expression from, Expression to) {
		this.from = from;
		this.to = to;
	}

	@Override public Object exec(ExecutionContext context) {
		Object fromVal = context.exec(from);
		Object toVal = context.exec(to);
		if (!(fromVal instanceof Integer) || !(toVal instanceof Integer))
			throw context.exception("Cannot create range form non-integer bounds {" + fromVal +", " + toVal + "}", this);
		Integer from = (Integer)fromVal;
		Integer to = (Integer)toVal;
		if (from > to)
			throw context.exception("Cannot create range. Bounds are inverse {" + from +", " + to + "}", this);

		return new Sequence(from, to);
	}
}
