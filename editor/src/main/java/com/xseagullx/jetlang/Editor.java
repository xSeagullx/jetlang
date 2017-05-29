package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.jvm.JavaBytecodeCompiler;
import com.xseagullx.jetlang.runtime.stack.ForkJoinExecutor;
import com.xseagullx.jetlang.runtime.stack.ParallelExecutor;
import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;
import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;
import com.xseagullx.jetlang.services.ActionManager;
import com.xseagullx.jetlang.services.ConfigService;
import com.xseagullx.jetlang.services.DocumentSnapshot;
import com.xseagullx.jetlang.services.HighlightTask;
import com.xseagullx.jetlang.services.HighlightingService;
import com.xseagullx.jetlang.services.Keymap;
import com.xseagullx.jetlang.services.RunTask;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.ui.Dialogs;
import com.xseagullx.jetlang.ui.EditPanel;
import com.xseagullx.jetlang.ui.FileManagingComponent;
import com.xseagullx.jetlang.ui.MiscPanel;
import com.xseagullx.jetlang.ui.OutPanel;
import com.xseagullx.jetlang.utils.FileUtils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/** High level component managing editor interactions. */
class Editor {
	private static final Logger log = Logger.getLogger(Editor.class.getName());

	private final ConfigService configService = ConfigService.create(new File("config.json"));
	private final StyleManager styleManager = StyleManager.create(configService.config.styles);
	private final ActionManager actionManager = new ActionManager();
	private HighlightingService highlightingService = new HighlightingService(styleManager);
	private EditorState editorState = new EditorState();
	private SimpleExecutionContext context;
	final AtomicReference<TokenInformationHolder> currentToken = new AtomicReference<>();

	private final OutPanel outputPanel;
	private final EditPanel editPanel;
	private final MiscPanel miscPanel;
	private final AtomicReference<CompletableFuture<Void>> lastProgramExecution = new AtomicReference<>();
	private volatile DocumentSnapshot lastSnapshot;

	Editor() throws IOException, FontFormatException {
		Dimension editPanelSize = new Dimension(configService.config.width, Math.round(configService.config.height * 0.6f));
		Dimension miscPanelSize = new Dimension(configService.config.width, 20);
		Dimension outPanelSize = new Dimension(configService.config.width, configService.config.height - editPanelSize.height - miscPanelSize.height);
		outputPanel = new OutPanel(styleManager, outPanelSize);
		miscPanel = new MiscPanel(editorState, miscPanelSize);
		editPanel = new EditPanel(styleManager, editPanelSize);
		editPanel.onChange(this::highlightAndRun);
		editPanel.setCaretPositionListener((line, col) -> {
			editorState.setLineNo(line);
			editorState.setColNo(col);
		});

		JFrame frame = createFrame();
		Keymap.register(actionManager);
		FileManagingComponent fileComponent = new FileManagingComponent(this::open, editorState);
		bindTitleAndState(frame);
		regiterActions(frame, fileComponent);
		new Timer(300, e -> {
			editPanel.showExecutionMarker(currentToken.get());
		}).start();
		editorState.setInteractiveMode(true);
	}

	private void bindTitleAndState(JFrame frame) {
		editorState.subscribe(() -> {
			File file = editorState.getFile();
			String title = file != null ? "File: " + file.getAbsolutePath() : "New file";
			if (editorState.isInteractiveMode())
				title += " : interactive ON";
			if (editorState.isSlowMode())
				title += " : slooooow mode ON";
			if (editorState.isShowThreads())
				title += " : show threads ON";
			if (editorState.isUseByteCodeCompiler())
				title += " : jvm compiler ON";
			frame.setTitle(title);
			}
		);
	}

	private void regiterActions(JFrame frame, FileManagingComponent fileComponent) {
		actionManager.register(ActionManager.Action.RUN, (action) -> runProgram(editPanel.getDocumentSnapshot()));
		actionManager.register(ActionManager.Action.TOGGLE_SLOW_MO, (action) -> editorState.setSlowMode(!editorState.isSlowMode()));
		actionManager.register(ActionManager.Action.TOGGLE_SHOW_THREADS, (action) -> editorState.setShowThreads(!editorState.isShowThreads()));
		actionManager.register(ActionManager.Action.TOGGLE_USE_BYTECODE_COMPILER, (action) -> editorState.setUseByteCodeCompiler(!editorState.isUseByteCodeCompiler()));
		actionManager.register(ActionManager.Action.TOGGLE_INTERACTIVE_MODE, (action) -> editorState.setInteractiveMode(!editorState.isInteractiveMode()));
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

	private void highlightAndRun() {
		this.currentToken.set(null);
		HighlightTask highlightTask = new HighlightTask(editPanel.getDocumentSnapshot(), highlightingService);
		CompletableFuture.supplyAsync(highlightTask).thenAccept(it -> {
				SwingUtilities.invokeLater(() -> {
					log.info("Applying highlighting results.");
					editPanel.applyHighlighting(it, highlightTask.getDocumentSnapshot());
				});
				if (editorState.isInteractiveMode()) {
					runProgram(highlightTask.getDocumentSnapshot());
				}
			}
		);
	}

	private void runProgram(DocumentSnapshot snapshot) {
		// Event thread
		synchronized (lastProgramExecution) {
			CompletableFuture<Void> lastExecution = lastProgramExecution.get();
			if (!editorState.isInteractiveMode() && lastExecution != null) {
				Dialogs.showMessage("You can run only one instance of program. Please wait or cancel old one with [ESC] key.");
				return;
			}
			if (lastExecution != null) {
				lastSnapshot = snapshot;
				this.context.stopExecution(null, null);
				return;
			}
		}

		SimpleExecutionContext context = new SimpleExecutionContext(new ForkJoinExecutor(ParallelExecutor.EXECUTOR_CHUNK_SIZE));
		JetLangCompiler compiler = editorState.isUseByteCodeCompiler() ? new JavaBytecodeCompiler() : new StackMachineCompiler();
		context.setExecutionListener(new EditorExecutionListener(this, styleManager, outputPanel, editorState.isSlowMode(), editorState.isShowThreads()));
		this.context = context;
		miscPanel.showProgressBar();

		CompletableFuture<Void> handle = CompletableFuture.supplyAsync(new RunTask(compiler, snapshot, context))
			.handle((res, ex) -> {
				ex = ex instanceof CompletionException ? ex.getCause() : ex;

				if (ex instanceof JetLangException) {
					JetLangException jetLangException = (JetLangException)ex;
					this.context.error(jetLangException.getDetailedMessage());
					this.currentToken.set(jetLangException.getElement());
				} else if (ex != null) {
					this.context.error(ex.getMessage());
					this.currentToken.set(null);
				} else {
					this.currentToken.set(null);
				}
				miscPanel.hideProgressBar();
				synchronized (lastProgramExecution) {
					lastProgramExecution.set(null);
					this.context = null;
					if (lastSnapshot != null) {
						DocumentSnapshot snapshotCopy = lastSnapshot;
						lastSnapshot = null;
						SwingUtilities.invokeLater(() -> runProgram(snapshotCopy));
					}
				}
				return null;
			});
		synchronized (lastProgramExecution) {
			lastProgramExecution.set(handle);
		}
	}

	private void stopProgram() {
		if (context != null) {
			context.stopExecution(null, null);
		}
	}
}
