package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

/** High level component managing editor interactions */
class Editor {
	// TODO Привязать ошибки выполнения к выражению их породившему.
	// TODO Добавить окно ошибок компиляции
	// TODO CTrl шорткаты
	// TODO Прогресс бар / нижнее меню

	private final StyleManager styleManager = new StyleManager();
	private final ActionManager actionManager = new ActionManager();
	private final RunService runService = new RunService();
	private HighlightingService highlightingService = new HighlightingService(styleManager);

	private OutPanel outputPanel;
	private EditPanel editPanel;

	Editor() throws IOException, FontFormatException {
		outputPanel = new OutPanel(styleManager);
		editPanel = new EditPanel(styleManager);
		editPanel.onChange(this::highlight);

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
		JPanel mainPanel = new JPanel(new BorderLayout());
		frame.setContentPane(mainPanel);
		mainPanel.add(editPanel.createComponent(frame), BorderLayout.CENTER);
		mainPanel.add(outputPanel.getComponent(), BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
		return frame;
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
				outputPanel.outDocument.insertString(outputPanel.outDocument.getLength(), error, styleManager.error);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
		};
		Consumer<String> showInOutputPane = (error) -> {
			try {
				outputPanel.outDocument.insertString(outputPanel.outDocument.getLength(), error, styleManager.main);
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
		outputPanel.print("Running...\n", styleManager.main);

		runService.execute(editPanel.getDocumentSnapshot(), simpleExecutionContext);
	}
}
