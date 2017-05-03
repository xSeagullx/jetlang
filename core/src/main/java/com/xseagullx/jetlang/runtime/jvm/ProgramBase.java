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
}
