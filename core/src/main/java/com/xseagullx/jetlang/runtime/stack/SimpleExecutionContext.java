package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;
import com.xseagullx.jetlang.TokenInformationHolder;
import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

class Frame {
	final TokenInformationHolder caller;
	final Map<String, Object> variables = new HashMap<>();

	Frame(TokenInformationHolder caller) {
		this.caller = caller;
	}
}

public class SimpleExecutionContext implements ExecutionContext {
	private final Stack<Frame> frames = new Stack<>();
	private final ParallelExecutor parallelExecutor;
	private final CompletableFuture<Void> executionOutcome;

	public SimpleExecutionContext(ParallelExecutor executor) {
		parallelExecutor = executor;
		executionOutcome = new CompletableFuture<>();

		TokenInformationHolder tokenInformationHolder = new TokenInformationHolder();
		tokenInformationHolder.setTokenInfo(Thread.currentThread().getName(), 0, 0);
		push(tokenInformationHolder);
	}

	/** Copy constructor. Uses shallow copying to drag common things across contexts. */
	private SimpleExecutionContext(SimpleExecutionContext parent) {
		executionOutcome = parent.executionOutcome;
		parallelExecutor = parent.parallelExecutor;
		push(parent.frames.peek().caller);
	}

	@Override public boolean isVariableDefined(String variableName) {
		return frames.peek().variables.containsKey(variableName);
	}

	@Override public void defineVariable(String name, Object value) {
		frames.peek().variables.put(name, value);
	}

	@Override public Object getVariable(String name) {
		return frames.peek().variables.get(name);
	}

	@Override public void push(TokenInformationHolder tokenInformation) {
		frames.push(new Frame(tokenInformation));
	}

	@Override public void pop() {
		frames.pop();
	}

	@SuppressWarnings("Duplicates")
	@Override public void exec(Statement statement) {
		try {
			handleCancellation(statement);
			statement.exec(this);
		}
		catch (JetLangException e) {
			stopExecution(e);
			throw e;
		}
		catch (Throwable e) {
			stopExecution(e);
			throw exception("Fatal runtime exception while executing " + statement + "\n" + e.getMessage(), statement);
		}
	}

	@SuppressWarnings("Duplicates")
	@Override public Object exec(Expression expression) {
		try {
			handleCancellation(expression);
			return expression.exec(this);
		}
		catch (JetLangException e) {
			stopExecution(e);
			throw e;
		}
		catch (Throwable e) {
			stopExecution(e);
			throw exception("Fatal runtime exception while executing " + expression + "\n" + e.getMessage(), expression);
		}
	}

	@Override public JetLangException exception(String message, TokenInformationHolder holder) {
		int stackTraceSize = frames.size();
		TokenInformationHolder[] stackTrace = IntStream.range(0, stackTraceSize)
			.mapToObj(i -> frames.get(stackTraceSize - 1 - i).caller)
			.toArray(TokenInformationHolder[]::new);
		JetLangException exception = new JetLangException(message, holder, stackTrace);
		error(exception.getDetailedMessage());
		return exception;
	}

	@Override public List<Object> map(List<Object> list, LambdaExpression lambda) {
		return parallelExecutor.map(this, list, lambda);
	}

	@Override public boolean stopExecution(Throwable e) {
		synchronized (executionOutcome) {
			if (executionOutcome.isDone())
				return false;
			else {
				Throwable exception = e == null ? exception("User requested a cancellation", null) : e;
				executionOutcome.completeExceptionally(exception);
				return true;
			}
		}
	}

	@Override public SimpleExecutionContext copy() {
		return new SimpleExecutionContext(this);
	}

	@Override public CompletableFuture<Void> executionOutcome() {
		return executionOutcome;
	}

	private void handleCancellation(TokenInformationHolder node) {
		if (executionOutcome.isDone())
			throw new JetLangException("Stop execution", null, new TokenInformationHolder[0]);
	}
}
