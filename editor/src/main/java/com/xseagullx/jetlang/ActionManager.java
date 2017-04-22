package com.xseagullx.jetlang;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class ActionManager {
	enum Action {
		NEW,
		SAVE,
		OPEN,
		QUIT,
		RUN
	}

	private final Map<Action, Consumer<Action>> actionMap = new HashMap<>();

	void fire(Action action) {
		Consumer<Action> actionConsumer = actionMap.get(action);
		actionConsumer.accept(action);
	}

	void register(Action action, Consumer<Action> callback) {
		actionMap.put(action, callback);
	}
}
