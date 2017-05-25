package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.TokenInformationHolder;
import com.xseagullx.jetlang.services.DocumentSnapshot;
import com.xseagullx.jetlang.services.HighlightTask;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.services.StyledChunk;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/** UI component responsible for showing code and applying highlighting results */
public class EditPanel implements ActionListener {
	private static final int DELAY_MS = 300;
	private final StyleManager styleManager;
	private final Dimension preferredSize;
	private final StyledDocument document;
	private BiConsumer<Integer, Integer> caretPositionListener;
	private Collection<ParseError> errors;
	private CaretListener caretListener;
	private Timer timer;
	private Runnable documentOnChangeListener;
	private TokenInformationHolder currentToken;

	public EditPanel(StyleManager styleManager, Dimension preferredSize) {
		this.styleManager = styleManager;
		this.preferredSize = preferredSize;
		this.timer = new Timer(DELAY_MS, this);
		document = new DefaultStyledDocument();
		document.addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
				scheduleUpdate();
			}

			@Override public void removeUpdate(DocumentEvent e) {
				scheduleUpdate();
			}

			private void scheduleUpdate() {
				if (!timer.isRunning())
					timer.start();
				else
					timer.restart();
			}

			@Override public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	public Component createComponent() {
		JEditorPane editorPane = new JEditorPane();
		BiConsumer<String, Point> errorTip = createErrorTip();
		editorPane.setCaretColor(styleManager.caretColor);
		editorPane.setEditorKit(new StyledEditorKit());
		editorPane.setDocument(document);
		caretListener = ignored -> {
			int caretOffset = editorPane.getCaretPosition();
			showErrorTip(editorPane, errorTip, caretOffset);

			if (caretPositionListener == null)
				return;

			Element line = document.getParagraphElement(caretOffset);
			int lineNo = getLineNoOfElement(line);
			caretPositionListener.accept(lineNo + 1, (caretOffset - line.getStartOffset()) + 1);
		};
		editorPane.addCaretListener(caretListener);
		JScrollPane jScrollPane = new JScrollPane(editorPane);
		jScrollPane.setPreferredSize(preferredSize);
		return jScrollPane;
	}

	private int getLineNoOfElement(Element line) {
		int lineNo = -1;
		for (int i = 0; i < document.getDefaultRootElement().getElementCount(); i++)
			if (line == document.getDefaultRootElement().getElement(i))
				lineNo = i;
		return lineNo;
	}

	public void onChange(Runnable documentOnChangeListener) {
		this.documentOnChangeListener = documentOnChangeListener;
	}

	public void setText(String text) {
		try {
			setErrors(null);
			document.remove(0, document.getLength());
			document.insertString(0, text, null);
		}
		catch (BadLocationException e) {
			throw new ThisShouldNeverHappenException(e);
		}
	}

	public void applyHighlighting(HighlightTask.HighlightingResults highlightingResults) {
		document.setCharacterAttributes(0, document.getLength(), new SimpleAttributeSet(), true);
		for (StyledChunk it : highlightingResults.styledChunks)
			document.setCharacterAttributes(it.offset, it.length, it.attributeSet, true);
		setErrors(highlightingResults.parseErrors);
	}

	/** Returns immutable copy of a document. */
	public DocumentSnapshot getDocumentSnapshot() {
		try {
			return new DocumentSnapshot(document.getText(0, document.getLength()));
		}
		catch (BadLocationException e) {
			throw new ThisShouldNeverHappenException(e);
		}
	}

	public void setCaretPositionListener(BiConsumer<Integer, Integer> caretPositionListener) {
		this.caretPositionListener = caretPositionListener;
	}

	public boolean isSnapshotValid(DocumentSnapshot documentSnapshot) {
		return Objects.equals(documentSnapshot.text, getDocumentSnapshot().text);
	}

	private void setErrors(Collection<ParseError> errors) {
		this.errors = errors;
		caretListener.caretUpdate(null);
	}

	private BiConsumer<String, Point> createErrorTip() {
		JWindow window = new JWindow();
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createLineBorder(styleManager.foregroundColor));
		window.setContentPane(contentPanel);
		window.setVisible(false);
		window.setAlwaysOnTop(true);

		Label errorLabel = new Label();
		contentPanel.setBackground(styleManager.backgroundColor);
		errorLabel.setForeground(styleManager.foregroundColor);
		contentPanel.add(errorLabel);
		return (message, position) -> {
			if (message == null || message.isEmpty())
				window.setVisible(false);
			else {
				window.setVisible(true);
				window.setLocation(position);
				errorLabel.setText(message);
				window.pack();
			}
		};
	}

	private void showErrorTip(JEditorPane editorPane, BiConsumer<String, Point> errorTip, int caretOffset) {
		String errorMessage = errorsAtPos(caretOffset).stream().map(ParseError::toString).collect(Collectors.joining("\n"));
		if (errorMessage.isEmpty())
			errorTip.accept(null, null);
		else {
			Point caretLocation;
			try {
				caretLocation = editorPane.modelToView(caretOffset).getLocation();
			}
			catch (BadLocationException e) {
				throw new ThisShouldNeverHappenException(e);
			}
			SwingUtilities.convertPointToScreen(caretLocation, editorPane);
			caretLocation.translate(4, 24);
			errorTip.accept(errorMessage, caretLocation);
		}
	}

	private List<ParseError> errorsAtPos(int caretPos) {
		return errors == null
					 ? Collections.emptyList()
					 : errors.stream().filter(it -> it.startOffset <= caretPos && it.endOffset >= caretPos).collect(Collectors.toList());
	}

	/** Will be called by timer every now and then after events like document or caret position update. */
	@Override public void actionPerformed(ActionEvent e) {
		timer.stop();
		showExecutionMarker(null);
		documentOnChangeListener.run();
	}

	public void showExecutionMarker(TokenInformationHolder token) {
		highlightToken(currentToken, styleManager.normalTokenBackground);
		currentToken = token;
		highlightToken(currentToken, styleManager.currentToken);
	}

	private void highlightToken(TokenInformationHolder currentToken, MutableAttributeSet style) {
		if (currentToken != null)
			document.setCharacterAttributes(currentToken.getOffset(), currentToken.getLength() + 1, style, false);
	}
}
