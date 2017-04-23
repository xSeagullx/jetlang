package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.Label;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** High level component managing editor interactions */
class Editor {
	// TODO Привязать ошибки выполнения к выражению их породившему.
	// TODO Добавить окно ошибок компиляции
	// TODO CTrl шорткаты для Windows.

	private final StyleManager styleManager = new StyleManager();
	private final ActionManager actionManager = new ActionManager();
	private final TaskManager taskManager = new TaskManager();
	private final RunService runService = new RunService(taskManager);
	private HighlightingService highlightingService = new HighlightingService(styleManager);
	private EditorState editorState = new EditorState();

	private OutPanel outputPanel;
	private EditPanel editPanel;

	Editor() throws IOException, FontFormatException {
		outputPanel = new OutPanel(styleManager);
		editPanel = new EditPanel(styleManager);
		editPanel.onChange(this::highlight);
		editPanel.setCaretPositionListener((line, col) -> {
			editorState.setLineNo(line);
			editorState.setColNo(col);
		});

		JFrame frame = createFrame();
		Keymap.register(actionManager);

		actionManager.register(ActionManager.Action.RUN, (action) -> runProgram());
		actionManager.register(ActionManager.Action.QUIT, (action) -> frame.dispose());
		actionManager.register(ActionManager.Action.OPEN, (action) -> frame.dispose());
		actionManager.register(ActionManager.Action.SAVE, (action) -> frame.dispose());
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel miscPanel = createMiscPanel();
		frame.setContentPane(miscPanel);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(editPanel.createComponent(), BorderLayout.CENTER);
		mainPanel.add(outputPanel.getComponent(), BorderLayout.SOUTH);
		miscPanel.add(mainPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	private JPanel createMiscPanel() {
		JPanel miscPanel = new JPanel(new BorderLayout());
		JPanel lowerPanel = new JPanel(new BorderLayout());
		Label caretPositionLabel = new Label();
		editorState.subscribe(() -> {
			caretPositionLabel.setText(editorState.getLineNo() + ":" + editorState.getColNo());
			lowerPanel.validate();
		});
		lowerPanel.add(caretPositionLabel, BorderLayout.EAST);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setBorderPainted(false);
		JLabel progressLabel = new JLabel();
		progressLabel.setVisible(false);
		progressBar.setVisible(false);
		lowerPanel.setPreferredSize(new Dimension(1024, 20));

		taskManager.subscribe((it) -> SwingUtilities.invokeLater(() -> {
			List<TaskExecution> runningTasks = taskManager.runningTasks().stream()
				.filter(task -> task.status == TaskExecution.Status.SCHEDULED || task.status == TaskExecution.Status.RUNNING)
				.collect(Collectors.toList());
			boolean nothingRunning = runningTasks.isEmpty();
			progressBar.setIndeterminate(!nothingRunning);
			progressBar.setVisible(!nothingRunning);
			progressLabel.setVisible(!nothingRunning);
			progressLabel.setText(runningTasks.size() == 1 ? runningTasks.get(0).getName() : runningTasks.size() + " processes: ");
			lowerPanel.validate();
		}));
		lowerPanel.add(progressLabel, BorderLayout.WEST);
		lowerPanel.add(progressBar, BorderLayout.CENTER);
		miscPanel.add(lowerPanel, BorderLayout.SOUTH);

		return miscPanel;
	}

	void open() throws Exception {
		String program = "var n = 150\n" +
			"var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))\n" +
			"var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
			"print \"pi = \"\n" +
			"out pi\n";
		editPanel.setText(program);
	}

	private void highlight() {
		// Increment highlight counter
		// Get text
		// Pass it to highlighter task
		// When ready
		// if task.counter ==  current.counter
		//  highlight document
		// else
		// discard changes (next highlight task is scheduled)
		// in case of error - show error.

		// Following code is used only for debug. //FIXME
		outputPanel.clear();
		Consumer<String> showErrorInOutputPane = (error) -> {
			try {
				outputPanel.document.insertString(outputPanel.document.getLength(), error, styleManager.error);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
		};
		Consumer<String> showInOutputPane = (error) -> {
			try {
				outputPanel.document.insertString(outputPanel.document.getLength(), error, styleManager.main);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
		};
		// end of debug code

		Collection<StyledChunk> highlighterResults = highlightingService.highlight(editPanel.getDocumentSnapshot(), showInOutputPane, showErrorInOutputPane);
		editPanel.applyHighlighting(highlighterResults);
	}

	private void runProgram() {
		SimpleExecutionContext simpleExecutionContext = new SimpleExecutionContext() { // TODO bind with outPane
			@Override public void print(Object value) {
				outputPanel.print(String.valueOf(value) + "\n", styleManager.main);
			}

			@Override public void error(String value) {
				outputPanel.print(String.valueOf(value) + "\n", styleManager.error);
			}
		};

		outputPanel.clear();

		runService.execute(editPanel.getDocumentSnapshot(), simpleExecutionContext);
	}
}
