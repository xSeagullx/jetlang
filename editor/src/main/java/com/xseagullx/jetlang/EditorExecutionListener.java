package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.ExecutionListener;
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.ui.OutPanel;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;

public class EditorExecutionListener implements ExecutionListener {
	private static final int SLOW_MO_DELAY_MS = 100;

	private final StyleManager styleManager;
	private final OutPanel outputPanel;
	private final boolean isSlowMode;
	private final boolean isShowThreads;
	private Editor editor;

	EditorExecutionListener(Editor editor, StyleManager styleManager, OutPanel outputPanel, boolean isSlowMode, boolean isShowThreads) {
		this.editor = editor;
		this.styleManager = styleManager;
		this.outputPanel = outputPanel;
		this.isSlowMode = isSlowMode;
		this.isShowThreads = isShowThreads;
		outputPanel.clear();
		if (isShowThreads)
			outputPanel.print("Internal thread info will be shown in all printed messages\n", styleManager.main);
	}

	@Override public void onExecute(SimpleExecutionContext context, TokenInformationHolder currentToken) {
		editor.currentToken.set(currentToken);
		if (isSlowMode)
			try {
				Thread.sleep(SLOW_MO_DELAY_MS);
			}
			catch (InterruptedException ignored) {
				// Just wake up
			}
	}

	@Override public void onPrint(SimpleExecutionContext context, Object value) {
		println(value, styleManager.main);
	}

	@Override public void onError(SimpleExecutionContext context, Object value) {
		println(value, styleManager.error);
	}

	@Override public void onVariableDefined(SimpleExecutionContext context, String name, Object value) {
		println(name + ":" + value.getClass().getSimpleName() + " = " + value, styleManager.main);
	}

	private void println(Object value, AttributeSet style) {
		String val = (isShowThreads ? Thread.currentThread().getName() + ": " : "") + String.valueOf(value) + "\n";
		SwingUtilities.invokeLater(() -> outputPanel.print(val, style));
	}
}
