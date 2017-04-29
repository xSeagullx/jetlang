package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.ExecutionListener;
import com.xseagullx.jetlang.runtime.stack.ForkJoinExecutor;
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;
import com.xseagullx.jetlang.services.ActionManager;
import com.xseagullx.jetlang.services.AlreadyRunningException;
import com.xseagullx.jetlang.services.HighlightTask;
import com.xseagullx.jetlang.services.HighlightingService;
import com.xseagullx.jetlang.services.Keymap;
import com.xseagullx.jetlang.services.RunService;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.services.TaskManager;
import com.xseagullx.jetlang.ui.Dialogs;
import com.xseagullx.jetlang.ui.EditPanel;
import com.xseagullx.jetlang.ui.FileManagingComponent;
import com.xseagullx.jetlang.ui.MiscPanel;
import com.xseagullx.jetlang.ui.OutPanel;
import com.xseagullx.jetlang.utils.FileUtils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/** High level component managing editor interactions. */
class Editor {
	private static final Logger log = Logger.getLogger(Editor.class.getName());
	private static final int SLOW_MO_DELAY_MS = 100;

	// TODO Парсинг конфига из JSON

	private final StyleManager styleManager = new StyleManager();
	private final ActionManager actionManager = new ActionManager();
	private final TaskManager taskManager = new TaskManager();
	private final RunService runService = new RunService(taskManager);
	private HighlightingService highlightingService = new HighlightingService(styleManager);
	private EditorState editorState = new EditorState();
	private SimpleExecutionContext context;

	private final OutPanel outputPanel;
	private final EditPanel editPanel;
	private final MiscPanel miscPanel;

	Editor() throws IOException, FontFormatException {
		outputPanel = new OutPanel(styleManager);
		miscPanel = new MiscPanel(editorState, taskManager);
		editPanel = new EditPanel(styleManager);
		editPanel.onChange(this::highlight);
		editPanel.setCaretPositionListener((line, col) -> {
			editorState.setLineNo(line);
			editorState.setColNo(col);
		});

		JFrame frame = createFrame();
		Keymap.register(actionManager);
		FileManagingComponent fileComponent = new FileManagingComponent(this::open, editorState);
		editorState.subscribe(() -> {
			File file = editorState.getFile();
			String title = file != null ? "File: " + file.getAbsolutePath() : "New file";
			if (editorState.isSlowMode())
				title += " : slooooow mode ON";
			frame.setTitle(title);
			}
		);

		actionManager.register(ActionManager.Action.RUN, (action) -> runProgram());
		actionManager.register(ActionManager.Action.TOGGLE_SLOW_MO, (action) -> editorState.setSlowMode(!editorState.isSlowMode()));
		actionManager.register(ActionManager.Action.STOP, (action) -> stopProgram());
		actionManager.register(ActionManager.Action.QUIT, (action) -> frame.dispose());
		actionManager.register(ActionManager.Action.OPEN, (action) -> fileComponent.openFileDialog(frame));
		actionManager.register(ActionManager.Action.SAVE, (action) -> fileComponent.saveFile(frame, editPanel.getDocumentSnapshot().text));
		actionManager.register(ActionManager.Action.CLOSE, (action) -> {
			editorState.setFile(null);
			editPanel.setText("");
		});
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Container miscPanelComponent = miscPanel.createComponent();
		frame.setContentPane(miscPanelComponent);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(editPanel.createComponent(), BorderLayout.CENTER);
		mainPanel.add(outputPanel.getComponent(), BorderLayout.SOUTH);
		miscPanelComponent.add(mainPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	void open(File file) {
		if (file != null) {
			try {
				String text = FileUtils.readAsUTF8String(file);
				editorState.setFile(file);
				editPanel.setText(text);
			}
			catch (IOException e) {
				Dialogs.showMessage("Error opening file: '" + file.getAbsolutePath() + "'." + e.getMessage());
			}
		}
	}

	private void highlight() {
		HighlightTask highlightTask = new HighlightTask(editPanel.getDocumentSnapshot(), highlightingService);
		taskManager.run(highlightTask).thenAccept(it -> {
				if (!editPanel.isSnapshotValid(highlightTask.getDocumentSnapshot())) {
					log.info("Highlighting results are discarded");
					return;
				}

				SwingUtilities.invokeLater(() -> {
					log.info("Applying highlighting results.");
					it.getFuture().thenAccept(editPanel::applyHighlighting);
				});
			}
		);
	}

	private void runProgram() {
		try {
			boolean isSlowMode = editorState.isSlowMode();
			SimpleExecutionContext context = new SimpleExecutionContext(new ForkJoinExecutor(100));
			context.setExecutionListener(new ExecutionListener() {
				@Override public void onExecute(SimpleExecutionContext context, TokenInformationHolder currentToken) {
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

				private void println(Object value, AttributeSet style) {
					String val = Thread.currentThread().getName() + ": " + String.valueOf(value) + "\n";
					SwingUtilities.invokeLater(() -> outputPanel.print(val, style));
				}
			});

			runService.execute(editPanel.getDocumentSnapshot(), context);
			outputPanel.clear();
			this.context = context;
		}
		catch (AlreadyRunningException e) {
			Dialogs.showMessage("You can run only one instance of program. Please wait or cancel old one with [ESC] key.");
		}
	}

	private void stopProgram() {
		if (context != null)
			context.stopExecution(null);
	}
}
