package com.xseagullx.jetlang.services;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Keymap {
	private static class Shortcut {
		private final int key;
		private final int modifiers;

		private Shortcut(int key, int modifiers) {
			this.key = key;
			this.modifiers = modifiers;
		}

		private Shortcut(int key) {
			this.key = key;
			this.modifiers = 0;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Shortcut shortcut = (Shortcut)o;
			return key == shortcut.key &&
				modifiers == shortcut.modifiers;
		}

		@Override public int hashCode() {
			return Objects.hash(key, modifiers);
		}
	}

	public static void register(ActionManager actionManager) {
		Map<Shortcut, ActionManager.Action> keymap = new HashMap<>();
		int oSSpecificModifier = "Mac OS X".equals(System.getProperty("os.name")) ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK;
		keymap.put(new Shortcut(KeyEvent.VK_ENTER, oSSpecificModifier), ActionManager.Action.RUN);
		keymap.put(new Shortcut(KeyEvent.VK_O, oSSpecificModifier), ActionManager.Action.OPEN);
		keymap.put(new Shortcut(KeyEvent.VK_S, oSSpecificModifier), ActionManager.Action.SAVE);
		keymap.put(new Shortcut(KeyEvent.VK_Q, oSSpecificModifier), ActionManager.Action.QUIT);
		keymap.put(new Shortcut(KeyEvent.VK_W, oSSpecificModifier), ActionManager.Action.CLOSE);
		keymap.put(new Shortcut(KeyEvent.VK_T, oSSpecificModifier), ActionManager.Action.TOGGLE_SLOW_MO);
		keymap.put(new Shortcut(KeyEvent.VK_I, oSSpecificModifier), ActionManager.Action.TOGGLE_SHOW_THREADS);
		keymap.put(new Shortcut(KeyEvent.VK_J, oSSpecificModifier), ActionManager.Action.TOGGLE_USE_BYTECODE_COMPILER);
		keymap.put(new Shortcut(KeyEvent.VK_ESCAPE), ActionManager.Action.STOP);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				Shortcut shortcut = new Shortcut(e.getKeyCode(), e.getModifiers());
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
