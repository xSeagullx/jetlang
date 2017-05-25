package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.TokenInformationHolder;

public interface ExecutionListener {
	default void onExecute(SimpleExecutionContext context, TokenInformationHolder currentToken) {
	}

	default void onPrint(SimpleExecutionContext context, Object value) {
	}

	default void onError(SimpleExecutionContext context, Object value) {
	}

	default void onVariableDefined(SimpleExecutionContext context, String name, Object value) {
	}
}
