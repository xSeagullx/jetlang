package com.xseagullx.jetlang.services;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ActionManager {
	public enum Action {
		CLOSE,
		SAVE,
		OPEN,
		QUIT,
		TOGGLE_SLOW_MO,
		TOGGLE_SHOW_THREADS,
		TOGGLE_USE_BYTECODE_COMPILER,
		TOGGLE_INTERACTIVE_MODE,
		STOP,
		RUN
	}

	private final Map<Action, Consumer<Action>> actionMap = new HashMap<>();

	void fire(Action action) {
		Consumer<Action> actionConsumer = actionMap.get(action);
		actionConsumer.accept(action);
	}

	public void register(Action action, Consumer<Action> callback) {
		actionMap.put(action, callback);
	}
}
