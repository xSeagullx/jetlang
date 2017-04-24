package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;
import com.xseagullx.jetlang.TokenInformationHolder;
import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.IntStream;

class Frame {
	final TokenInformationHolder caller;
	final Map<String, Object> variables = new HashMap<>();

	Frame(TokenInformationHolder caller) {
		this.caller = caller;
	}
}

public class SimpleExecutionContext implements ExecutionContext {
	private Stack<Frame> frames = new Stack<>();

	protected SimpleExecutionContext() {
		TokenInformationHolder tokenInformationHolder = new TokenInformationHolder();
		tokenInformationHolder.setTokenInfo("root", 0, 0);
		push(tokenInformationHolder);
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

	@Override public void exec(Statement statement) {
		try {
			statement.exec(this);
		}
		catch (JetLangException e) {
			throw e;
		}
		catch (Throwable e) {
			throw exception("Fatal runtime exception while executing " + statement + "\n" + e.getMessage(), statement);
		}
	}

	@Override public Object exec(Expression expression) {
		try {
			return expression.exec(this);
		}
		catch (JetLangException e) {
			throw e;
		}
		catch (Throwable e) {
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
}
