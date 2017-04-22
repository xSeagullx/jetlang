package com.xseagullx.jetlang;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashMap;

class Keymap {
	static void register(ActionManager actionManager) {
		HashMap<String, ActionManager.Action> keymap = new HashMap<>();
		keymap.put("⌘+⏎", ActionManager.Action.RUN);
		keymap.put("⌘+O", ActionManager.Action.OPEN);
		keymap.put("⌘+S", ActionManager.Action.SAVE);
		keymap.put("⌘+Q", ActionManager.Action.QUIT);

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
}
