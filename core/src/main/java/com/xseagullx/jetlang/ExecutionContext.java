package com.xseagullx.jetlang;

public interface ExecutionContext {
	boolean isVariableDefined(String variableName);
	void defineVariable(String name, Object value);
	Object getVariable(String text);

	/** Pushes new scope to the context. (entered lambda) */
	void push();
	/** Pops current scope from context. (exited lambda) */
	void pop();

	default void print(Object value) {
		System.out.println(value);
	}

	default void error(String value) {
		System.err.println(value);
	}
}
