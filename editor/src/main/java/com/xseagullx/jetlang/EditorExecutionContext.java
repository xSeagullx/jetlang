package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.ui.OutPanel;

import javax.swing.SwingUtilities;

public class EditorExecutionContext extends SimpleExecutionContext {
	private final OutPanel outputPanel;
	private final StyleManager styleManager;

	EditorExecutionContext(OutPanel outputPanel, StyleManager styleManager) {
		this.outputPanel = outputPanel;
		this.styleManager = styleManager;
	}

	@Override public void print(Object value) {
		SwingUtilities.invokeLater(() -> outputPanel.print(String.valueOf(value) + "\n", styleManager.main));
	}

	@Override public void error(String value) {
		SwingUtilities.invokeLater(() -> outputPanel.print(String.valueOf(value) + "\n", styleManager.error));
	}
}
