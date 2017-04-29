package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.EditorState;
import com.xseagullx.jetlang.services.TaskExecution;
import com.xseagullx.jetlang.services.TaskManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.util.List;
import java.util.stream.Collectors;

public class MiscPanel {
	private final EditorState editorState;
	private final TaskManager taskManager;
	private final Dimension preferredSize;

	public MiscPanel(EditorState editorState, TaskManager taskManager, Dimension preferredSize) {
		this.editorState = editorState;
		this.taskManager = taskManager;
		this.preferredSize = preferredSize;
	}

	public Container createComponent() {
		JPanel miscPanel = new JPanel(new BorderLayout());
		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.setPreferredSize(preferredSize);
		lowerPanel.add(createCaretPositionLable(lowerPanel), BorderLayout.EAST);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setBorderPainted(false);
		progressBar.setVisible(false);

		JLabel progressLabel = new JLabel();
		progressLabel.setVisible(false);

		taskManager.subscribe((it) -> SwingUtilities.invokeLater(() -> updateLowerPanel(lowerPanel, progressBar, progressLabel)));
		lowerPanel.add(progressLabel, BorderLayout.WEST);
		lowerPanel.add(progressBar, BorderLayout.CENTER);
		miscPanel.add(lowerPanel, BorderLayout.SOUTH);

		return miscPanel;
	}

	private Label createCaretPositionLable(JPanel lowerPanel) {
		Label caretPositionLabel = new Label();
		editorState.subscribe(() -> {
			caretPositionLabel.setText(editorState.getLineNo() + ":" + editorState.getColNo());
			lowerPanel.validate();
		});
		return caretPositionLabel;
	}

	private void updateLowerPanel(JPanel lowerPanel, JProgressBar progressBar, JLabel progressLabel) {
		List<TaskExecution> runningTasks = taskManager.runningTasks().stream()
			.filter(task -> task.status == TaskExecution.Status.SCHEDULED || task.status == TaskExecution.Status.RUNNING)
			.collect(Collectors.toList());
		boolean nothingRunning = runningTasks.isEmpty();
		progressBar.setIndeterminate(!nothingRunning);
		progressBar.setVisible(!nothingRunning);
		progressLabel.setVisible(!nothingRunning);
		progressLabel.setText(runningTasks.size() == 1 ? runningTasks.get(0).getName() : runningTasks.size() + " processes: ");
		lowerPanel.validate();
	}
}
