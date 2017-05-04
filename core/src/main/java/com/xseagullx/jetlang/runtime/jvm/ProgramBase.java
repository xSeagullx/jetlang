package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ProgramBase implements Runnable {
	public ExecutionContext context;

	protected void out(Object o) {
		context.print(o);
	}

	protected void err(Object o) {
		context.error(String.valueOf(o));
	}

	protected Object plus(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() + ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() + ((Number)b).intValue();
	}

	protected Object minus(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() - ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() - ((Number)b).intValue();
	}

	protected Object mul(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() * ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() * ((Number)b).intValue();
	}

	protected Object div(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() / ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() / ((Number)b).intValue();
	}

	protected Object pow(Object a, Object b) {
		return Math.pow(((Number)a).doubleValue(), ((Number)b).doubleValue());
	}

	protected Object newRange(Object from, Object to) {
		// Todo verify
		return new Sequence((Integer)from, (Integer)to);
	}

	protected Object map(Object sequence, Function<Object, Object> lambda) {
		return new Sequence(((Sequence)sequence).list.stream().map(lambda).collect(Collectors.toList()));
	}

	private boolean hasDouble(Object a, Object b) {
		return a instanceof Double || b instanceof Double;
	}
}
