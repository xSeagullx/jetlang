package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;
import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

class Frame {
	final String caller;
	final Map<String, Object> variables = new HashMap<>();

	Frame(String caller) {
		this.caller = caller;
	}
}

public class SimpleExecutionContext implements ExecutionContext {
	private Stack<Frame> frames = new Stack<>();

	protected SimpleExecutionContext() {
		push();
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

	@Override public void push() {
		frames.push(new Frame("lambda"));
	}

	@Override public void pop() {
		frames.pop();
	}

	@Override public void exec(Statement statement) {
		statement.exec(this);
	}

	@Override public Object exec(Expression expression) {
		try {
			return expression.exec(this);
		}
		catch (JetLangException e) {
			error(e.getMessage());
			String trace = this.frames.stream().map(i -> i.caller).collect(Collectors.joining("\n"));
			error(trace);
			throw e;
		}
	}
}
