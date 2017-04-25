package com.xseagullx.jetlang.services;

import java.awt.KeyboardFocusManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.awt.event.KeyEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.KEY_RELEASED;
import static java.awt.event.KeyEvent.META_MASK;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_W;

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
		int oSSpecificModifier = "Mac OS X".equals(System.getProperty("os.name")) ? META_MASK : CTRL_MASK;
		keymap.put(new Shortcut(VK_ENTER, oSSpecificModifier), ActionManager.Action.RUN);
		keymap.put(new Shortcut(VK_O, oSSpecificModifier), ActionManager.Action.OPEN);
		keymap.put(new Shortcut(VK_S, oSSpecificModifier), ActionManager.Action.SAVE);
		keymap.put(new Shortcut(VK_Q, oSSpecificModifier), ActionManager.Action.QUIT);
		keymap.put(new Shortcut(VK_W, oSSpecificModifier), ActionManager.Action.CLOSE);
		keymap.put(new Shortcut(VK_T, oSSpecificModifier), ActionManager.Action.TOGGLE_SLOW_MO);
		keymap.put(new Shortcut(VK_ESCAPE), ActionManager.Action.STOP);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getID() == KEY_RELEASED) {
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
