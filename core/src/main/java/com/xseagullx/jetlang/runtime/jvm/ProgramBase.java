package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Sequence;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ProgramBase implements Runnable {
	public ExecutionContext context;

	protected final void out(Object o) {
		context.print(o);
	}

	protected final void err(Object o) {
		context.error(String.valueOf(o));
	}

	protected final Object negate(Object a) {
		if (a instanceof Double)
			return -((Double)a);
		else
			return -((Integer)a);
	}

	protected final Object plus(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() + ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() + ((Number)b).intValue();
	}

	protected final Object minus(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() - ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() - ((Number)b).intValue();
	}

	protected final Object mul(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() * ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() * ((Number)b).intValue();
	}

	protected final Object div(Object a, Object b) {
		if (hasDouble(a, b))
			return ((Number)a).doubleValue() / ((Number)b).doubleValue();
		else
			return ((Number)a).intValue() / ((Number)b).intValue();
	}

	protected final Object pow(Object a, Object b) {
		return Math.pow(((Number)a).doubleValue(), ((Number)b).doubleValue());
	}

	protected final Object newRange(Object from, Object to) {
		// Todo verify
		return new Sequence((Integer)from, (Integer)to);
	}

	protected final Object map(Object sequence, Function<Object, Object> lambda) {
		return new Sequence(((Sequence)sequence).list.stream().map(lambda).collect(Collectors.toList()));
	}

	protected final  Object reduce(Object sequence, Object initial, BiFunction<Object, Object, Object> lambda) {
		return ((Sequence)sequence).list.stream().reduce(initial, lambda::apply);
	}

	private boolean hasDouble(Object a, Object b) {
		return a instanceof Double || b instanceof Double;
	}
}
