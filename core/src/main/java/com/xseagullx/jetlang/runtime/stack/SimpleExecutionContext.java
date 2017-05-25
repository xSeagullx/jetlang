package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;
import com.xseagullx.jetlang.Sequence;
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
	private ExecutionListener executionListener;

	public SimpleExecutionContext(ParallelExecutor executor) {
		parallelExecutor = executor;
		executionOutcome = new CompletableFuture<>();

		TokenInformationHolder tokenInformationHolder = new TokenInformationHolder();
		tokenInformationHolder.setTokenInfo("main", 1, 1, 0, 0);
		push(tokenInformationHolder);
	}

	/** Copy constructor. Uses shallow copying to drag common things across contexts. */
	private SimpleExecutionContext(SimpleExecutionContext parent) {
		executionOutcome = parent.executionOutcome;
		parallelExecutor = parent.parallelExecutor;
		executionListener = parent.executionListener;
		push(parent.frames.peek().caller);
	}

	@Override public boolean isVariableDefined(String variableName) {
		return frames.peek().variables.containsKey(variableName);
	}

	@Override public void defineVariable(String name, Object value) {
		frames.peek().variables.put(name, value);
		if (executionListener != null && frames.size() == 1)
			executionListener.onVariableDefined(this, name, value);
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
			if (executionListener != null)
				executionListener.onExecute(this, statement);
			statement.exec(this);
		}
		catch (JetLangException e) {
			stopExecution(e, statement);
			throw e;
		}
		catch (Throwable e) {
			if (stopExecution(e, statement) == null)
				throw exception("Fatal runtime exception during execution\n" + e.getMessage(), statement);
			throw e;
		}
	}

	@SuppressWarnings("Duplicates")
	@Override public Object exec(Expression expression) {
		try {
			handleCancellation(expression);
			if (executionListener != null)
				executionListener.onExecute(this, expression);
			return expression.exec(this);
		}
		catch (JetLangException e) {
			stopExecution(e, expression);
			throw e;
		}
		catch (Throwable e) {
			if (stopExecution(e, expression) == null)
				throw exception("Fatal runtime exception during execution\n" + e.getMessage(), expression);
			throw e;
		}
	}

	@Override public JetLangException exception(String message, TokenInformationHolder holder) {
		int stackTraceSize = frames.size();
		TokenInformationHolder[] stackTrace = IntStream.range(0, stackTraceSize)
			.mapToObj(i -> frames.get(stackTraceSize - 1 - i).caller)
			.toArray(TokenInformationHolder[]::new);
		JetLangException exception = new JetLangException(message, holder, stackTrace);
		executionOutcome.completeExceptionally(exception);
		return exception;
	}

	@Override public Object reduce(Sequence sequence, Object initialValue, LambdaExpression lambda) {
		return parallelExecutor.reduce(this, sequence.list, initialValue, lambda);
	}

	@Override public List<Object> map(Sequence sequence, LambdaExpression lambda) {
		return parallelExecutor.map(this, sequence.list, lambda);
	}

	@Override public CompletableFuture<Void> stopExecution(Throwable e, TokenInformationHolder currentToken) {
		synchronized (executionOutcome) {
			if (executionOutcome.isDone())
				return null;
			else {
				Throwable exception = e == null ? exception("User requested a cancellation", currentToken) : e;
				if (exception instanceof JetLangException)
					executionOutcome.completeExceptionally(exception);
				else {
					executionOutcome.completeExceptionally(exception(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName(), currentToken));
				}
				return executionOutcome;
			}
		}
	}

	@Override public SimpleExecutionContext copy() {
		return new SimpleExecutionContext(this);
	}

	@Override public CompletableFuture<Void> getExecutionOutcome() {
		return executionOutcome;
	}

	@Override public void print(Object value) {
		if (executionListener != null)
			executionListener.onPrint(this, value);
	}

	@Override public void error(String value) {
		if (executionListener != null)
			executionListener.onError(this, value);
	}

	private void handleCancellation(TokenInformationHolder node) {
		if (executionOutcome.isDone())
			throw new JetLangException("Stop execution", node, new TokenInformationHolder[0]);
	}

	public void setExecutionListener(ExecutionListener listener) {
		this.executionListener = listener;
	}
}
