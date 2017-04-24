package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.services.DocumentSnapshot;
import com.xseagullx.jetlang.services.HighlightTask;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.services.StyledChunk;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/** UI component responsible for showing code and applying highlighting results */
public class EditPanel {
	private final StyleManager styleManager;
	private final StyledDocument document;
	private BiConsumer<Integer, Integer> caretPositionListener;
	private Collection<ParseError> errors;

	public EditPanel(StyleManager styleManager) {
		this.styleManager = styleManager;
		document = new DefaultStyledDocument();
	}

	public Component createComponent() {
		JEditorPane editorPane = new JEditorPane();
		JWindow window = new JWindow();
		JPanel contentPane = new JPanel();
		window.setContentPane(contentPane);
		Label errorLabel = new Label("");
		contentPane.add(errorLabel);
		window.setVisible(false);
		window.pack();
		window.setAlwaysOnTop(true);
		editorPane.setCaretColor(styleManager.caretColor);
		editorPane.setEditorKit(new StyledEditorKit());
		editorPane.setDocument(document);
		editorPane.addCaretListener(e -> {
			if (caretPositionListener == null)
				return;

			int caretOffset = editorPane.getCaretPosition();
			try {
				Point caretLocation = editorPane.modelToView(caretOffset).getLocation();
				SwingUtilities.convertPointToScreen(caretLocation, editorPane);
				caretLocation.translate(0, 22);
				window.setLocation(caretLocation);
				List<ParseError> errors = errorsAtPos(caretOffset);
				window.setVisible(!errors.isEmpty());
				if (!errors.isEmpty()) {
					String errorTip = errors.stream().map(ParseError::toString).collect(Collectors.joining("\n"));
					errorLabel.setText(errorTip);
					window.pack();
				}
			}
			catch (BadLocationException e1) {
				throw new ThisShouldNeverHappenException(e1);
			}

			Element line = document.getParagraphElement(caretOffset);
			int lineNo = -1;
			for (int i = 0; i < document.getDefaultRootElement().getElementCount(); i++)
				if (line == document.getDefaultRootElement().getElement(i))
					lineNo = i;
			caretPositionListener.accept(lineNo + 1, (caretOffset - line.getStartOffset()) + 1);
		});
		JScrollPane jScrollPane = new JScrollPane(editorPane);
		jScrollPane.setPreferredSize(new Dimension(1024, 768));
		return jScrollPane;
	}

	private List<ParseError> errorsAtPos(int caretPos) {
		return errors.stream().filter(it -> it.startOffset <= caretPos && it.endOffset >= caretPos).collect(Collectors.toList());
	}

	public void onChange(Runnable requestHighlight) {
		document.addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(requestHighlight);
			}

			@Override public void removeUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(requestHighlight);
			}

			@Override public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	public void setText(String text) {
		try {
			document.remove(0, document.getLength());
			document.insertString(0, text, null);
		}
		catch (BadLocationException e) {
			throw new ThisShouldNeverHappenException(e);
		}
	}

	public void applyHighlighting(HighlightTask.HighlightingResults highlightingResults) {
		for (StyledChunk it : highlightingResults.styledChunks)
			document.setCharacterAttributes(it.offset, it.length, it.attributeSet, true);
		errors = highlightingResults.parseErrors;
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
}
