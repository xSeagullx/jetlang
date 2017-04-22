package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.SimpleExecutionContext;
import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyledEditorKit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class Editor {
	private final Styles styles;
	private final ActionManager actionManager = new ActionManager();
	private final DefaultStyledDocument inDocument;
	private final DefaultStyledDocument outDocument;

	Editor() throws IOException, FontFormatException {
		inDocument = new DefaultStyledDocument();
		outDocument = new DefaultStyledDocument();
		styles = new Styles(inDocument);
		inDocument.addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(Editor.this::highlight);
			}

			@Override public void removeUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(Editor.this::highlight);
			}

			@Override public void changedUpdate(DocumentEvent e) {
			}
		});
		JFrame frame = createFrame();
		Keymap.register(actionManager);

		actionManager.register(ActionManager.Action.RUN, (action) -> execute(getEditorContent()));
		actionManager.register(ActionManager.Action.QUIT, (action) -> frame.dispose());
		actionManager.register(ActionManager.Action.OPEN, (action) -> frame.dispose());
		actionManager.register(ActionManager.Action.SAVE, (action) -> frame.dispose());
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel(new BorderLayout());
		frame.setContentPane(mainPanel);
		mainPanel.add(createEditorPane(frame), BorderLayout.CENTER);
		mainPanel.add(createOutputPanel(), BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	private Component createOutputPanel() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setCaretColor(Color.decode("#bbbbbb"));
		editorPane.setEditorKit(new StyledEditorKit());
		editorPane.setDocument(outDocument);
		JScrollPane jScrollPane = new JScrollPane(editorPane);
		jScrollPane.setPreferredSize(new Dimension(1024, 200));
		return jScrollPane;
	}

	private Component createEditorPane(JFrame frame) {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setCaretColor(Color.decode("#bbbbbb"));
		editorPane.setEditorKit(new StyledEditorKit());
		editorPane.setDocument(inDocument);
		editorPane.addCaretListener(e -> {
			int caretOffset = editorPane.getCaretPosition();
			Element line = inDocument.getParagraphElement(caretOffset);
			int lineNo = -1;
			for (int i = 0; i < inDocument.getDefaultRootElement().getElementCount(); i++)
				if (line == inDocument.getDefaultRootElement().getElement(i))
					lineNo = i;
			frame.setTitle("Caret: " + editorPane.getCaretPosition() + " " + (lineNo + 1) + ":" + (caretOffset - line.getStartOffset() + 1));
		});
		JScrollPane jScrollPane = new JScrollPane(editorPane);
		jScrollPane.setPreferredSize(new Dimension(1024, 768));
		return jScrollPane;
	}

	void open() throws Exception {
		String program = "var n = 150\n" +
			"var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))\n" +
			"var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
			"print \"pi = \"\n" +
			"out pi\n";
		inDocument.insertString(0, program, styles.main);
	}

	private Set<Integer> KEYWORDS = new HashSet<>(Arrays.asList(
		JetLangLexer.KW_VAR,
		JetLangLexer.KW_MAP,
		JetLangLexer.KW_REDUCE,
		JetLangLexer.KW_OUT,
		JetLangLexer.KW_PRINT
	));

	private void highlight() {
		try {
			outDocument.remove(0, outDocument.getLength());
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}

		JetLangLexer lexer = new JetLangLexer(CharStreams.fromString(getEditorContent()));
		lexer.removeErrorListeners(); // remove default listeners, that print error to stderr.

		lexer.addErrorListener(new BaseErrorListener() {
			@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				try {
					outDocument.insertString(outDocument.getLength(), line + ":" + (charPositionInLine + 1) + " ERROR: " + e + "\n", styles.error);
				}
				catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				int lineOffset = inDocument.getDefaultRootElement().getElement(line - 1).getStartOffset();
				inDocument.setCharacterAttributes(lineOffset + charPositionInLine, 1, styles.error, true);
			}
		});
		lexer.getAllTokens().forEach((it) -> {
			Style style;
			if (KEYWORDS.contains(it.getType()))
				style = styles.keyword;
			else if (it.getType() == JetLangLexer.STRING)
				style = styles.string;
			else if (it.getType() == JetLangLexer.INTEGER || it.getType() == JetLangLexer.REAL_NUMBER)
				style = styles.number;
			else
				style = styles.main;

			try {
				outDocument.insertString(outDocument.getLength(), it.getStartIndex() + ":" + it.getStopIndex() + " " + JetLangLexer.VOCABULARY.getDisplayName(it.getType()) + " " + it.getText() + "\n", styles.main);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
			inDocument.setCharacterAttributes(it.getStartIndex(), it.getText().length(), style, true);
		});

		highlightParseErrors();
	}

	private void highlightParseErrors() {
		JetLangLexer lexer = new JetLangLexer(CharStreams.fromString(getEditorContent()));
		JetLangParser parser = new JetLangParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener() {
			@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				Token token = (Token)offendingSymbol;
				inDocument.setCharacterAttributes(token.getStartIndex(), token.getText() != null ? token.getText().length() : 1, styles.error, true);
			}
		});
		parser.program();
	}

	private String getEditorContent() {
		Segment segment = new Segment();
		try {
			inDocument.getText(0, inDocument.getLength(), segment);
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		return segment.toString();
	}

	private void execute(String program) {
		SimpleExecutionContext simpleExecutionContext = new SimpleExecutionContext() {
			@Override public void print(Object value) {
				try {
					outDocument.insertString(outDocument.getLength(), String.valueOf(value) + "\n", styles.main);
				}
				catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			@Override public void error(String value) {
				try {
					outDocument.insertString(outDocument.getLength(), String.valueOf(value) + "\n", styles.error);
				}
				catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		};

		try {
			outDocument.remove(0, outDocument.getLength());
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}

		new StackMachineCompiler().parse(program).execute(simpleExecutionContext);
	}
}
