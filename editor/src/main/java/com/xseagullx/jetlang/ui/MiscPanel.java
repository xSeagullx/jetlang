package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.EditorState;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.util.function.Consumer;

public class MiscPanel {
	private final EditorState editorState;
	private final Dimension preferredSize;
	private Consumer<Boolean> updateProgressBar;

	public MiscPanel(EditorState editorState, Dimension preferredSize) {
		this.editorState = editorState;
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

		updateProgressBar = (it) -> updateLowerPanel(lowerPanel, progressBar, progressLabel, it);
		lowerPanel.add(progressLabel, BorderLayout.WEST);
		lowerPanel.add(progressBar, BorderLayout.CENTER);
		miscPanel.add(lowerPanel, BorderLayout.SOUTH);

		return miscPanel;
	}

	public void showProgressBar() {
		updateProgressBar.accept(true);
	}

	public void hideProgressBar() {
		updateProgressBar.accept(false);
	}

	private Label createCaretPositionLable(JPanel lowerPanel) {
		Label caretPositionLabel = new Label();
		editorState.subscribe(() -> {
			caretPositionLabel.setText(editorState.getLineNo() + ":" + editorState.getColNo());
			lowerPanel.validate();
		});
		return caretPositionLabel;
	}

	private void updateLowerPanel(JPanel lowerPanel, JProgressBar progressBar, JLabel progressLabel, boolean isRunning) {
		progressBar.setIndeterminate(isRunning);
		progressBar.setVisible(isRunning);
		progressLabel.setVisible(isRunning);
		progressLabel.setText("Processing");
		lowerPanel.validate();
	}
}
