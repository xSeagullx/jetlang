package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SimpleExecutionContext implements ExecutionContext {
	private Stack<Map<String, Object>> variables = new Stack<>();

	SimpleExecutionContext() {
		push();
	}

	@Override public boolean isVariableDefined(String variableName) {
		return variables.peek().containsKey(variableName);
	}

	@Override public void defineVariable(String name, Object value) {
		variables.peek().put(name, value);
	}

	@Override public Object getVariable(String name) {
		return variables.peek().get(name);
	}

	@Override public void push() {
		variables.push(new HashMap<>());
	}

	@Override public void pop() {
		variables.pop();
	}
}
