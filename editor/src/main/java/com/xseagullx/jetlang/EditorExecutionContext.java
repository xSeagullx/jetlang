package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.ForkJoinExecutor;
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;
import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.ui.OutPanel;

import javax.swing.SwingUtilities;

public class EditorExecutionContext extends SimpleExecutionContext {
	private static final int SLOW_MO_DELAY_MS = 100;

	private final OutPanel outputPanel;
	private final StyleManager styleManager;
	private final boolean slowMode;

	EditorExecutionContext(OutPanel outputPanel, StyleManager styleManager, boolean slowMode) {
		super(new ForkJoinExecutor(100));
		this.outputPanel = outputPanel;
		this.styleManager = styleManager;
		this.slowMode = slowMode;
	}

	@Override public void print(Object value) {
		SwingUtilities.invokeLater(() -> outputPanel.print(String.valueOf(value) + "\n", styleManager.main));
	}

	@Override public void error(String value) {
		SwingUtilities.invokeLater(() -> outputPanel.print(String.valueOf(value) + "\n", styleManager.error));
	}

	@Override public void exec(Statement statement) {
		delayExecution();
		super.exec(statement);
	}

	@Override public Object exec(Expression expression) {
		delayExecution();
		return super.exec(expression);
	}

	private void delayExecution() {
		if (slowMode) {
			try {
				Thread.sleep(SLOW_MO_DELAY_MS);
			}
			catch (InterruptedException e) {
				// just wake up
			}
		}
	}
}
