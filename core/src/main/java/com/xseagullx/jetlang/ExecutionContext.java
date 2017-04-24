package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;

public interface ExecutionContext {
	boolean isVariableDefined(String variableName);
	void defineVariable(String name, Object value);
	Object getVariable(String text);

	/** Pushes new scope to the context. (entered lambda) */
	void push(TokenInformationHolder tokenInformation);
	/** Pops current scope from context. (exited lambda) */
	void pop();

	default void print(Object value) {
		System.out.println(value);
	}

	default void error(String value) {
		System.err.println(value);
	}

	void exec(Statement statement);
	Object exec(Expression expression);

	JetLangException exception(String message, TokenInformationHolder holder);

	default void cancel() {
		throw new UnsupportedOperationException();
	}
}
