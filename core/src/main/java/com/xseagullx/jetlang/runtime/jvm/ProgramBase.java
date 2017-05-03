package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.ExecutionContext;

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

	private boolean hasDouble(Object a, Object b) {
		return a instanceof Double || b instanceof Double;
	}
}
