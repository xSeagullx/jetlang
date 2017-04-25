package com.xseagullx.jetlang.services;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class Keymap {
	public static void register(ActionManager actionManager) {
		Map<String, ActionManager.Action> keymap = new HashMap<>();
		bind(keymap, ActionManager.Action.RUN, "⌘+⏎", "Ctrl+Enter");
		bind(keymap, ActionManager.Action.OPEN, "⌘+O", "Ctrl+O");
		bind(keymap, ActionManager.Action.SAVE, "⌘+S", "Ctrl+S");
		bind(keymap, ActionManager.Action.QUIT, "⌘+Q", "Ctrl+Q");
		bind(keymap, ActionManager.Action.CLOSE, "⌘+W", "Ctrl+W");
		bind(keymap, ActionManager.Action.TOGGLE_SLOW_MO, "⌘+T", "Ctrl+T");
		bind(keymap, ActionManager.Action.STOP, "⎋", "Escape");

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				String modifierText = KeyEvent.getKeyModifiersText(e.getModifiers());
				String keyText = KeyEvent.getKeyText(e.getKeyCode());
				String shortcut = ((!"".equals(modifierText)) ? modifierText + "+" : "") + keyText;
				ActionManager.Action action = keymap.get(shortcut);
				if (action != null) {
					actionManager.fire(action);
					return true;
				}
			}
			return false;
		});
	}

	private static void bind(Map<String, ActionManager.Action> keymap, ActionManager.Action action, String... shortcuts) {
		for (String shortcut : shortcuts)
			keymap.put(shortcut, action);
	}
}
