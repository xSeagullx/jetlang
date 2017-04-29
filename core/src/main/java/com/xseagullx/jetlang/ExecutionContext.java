package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
		System.err.println(Thread.currentThread().getName() + " " + value);
	}

	void exec(Statement statement);
	Object exec(Expression expression);

	/**
	 * Constructs JetLang exception with proper (JetLang) stacktrace.
	 * Prints error in via {@link #print(Object)} method.
	 */
	JetLangException exception(String message, TokenInformationHolder holder);

	List<Object> map(Sequence list, LambdaExpression lambda);
	Object reduce(Sequence sequence, Object initialValue, LambdaExpression lambda);

	/** Stops current execution.
	 * Optional operation: does nothing if execution is not running.
	 * @param e Exception, leading to execution to be stopped. Can be null. Will be ignored, if execution is already stopped.
	 * @return true if execution has been stopped.
	 */
	boolean stopExecution(Throwable e);

	ExecutionContext copy();

	CompletableFuture<Void> getExecutionOutcome();

}
