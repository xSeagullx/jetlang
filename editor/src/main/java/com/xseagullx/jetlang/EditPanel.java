package com.xseagullx.jetlang;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
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
import java.util.Collection;
import java.util.function.BiConsumer;

class DocumentSnapshot {
	final String text;
	final int lineOffsets[];

	DocumentSnapshot(String text, int[] lineOffsets) {
		this.text = text;
		this.lineOffsets = lineOffsets;
	}
}

class EditPanel {
	private final StyleManager styleManager;
	private final StyledDocument document;
	private BiConsumer<Integer, Integer> caretPositionListener;

	EditPanel(StyleManager styleManager) {
		this.styleManager = styleManager;
		document = new DefaultStyledDocument();
	}

	Component createComponent() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setCaretColor(styleManager.caretColor);
		editorPane.setEditorKit(new StyledEditorKit());
		editorPane.setDocument(document);
		editorPane.addCaretListener(e -> {
			if (caretPositionListener == null)
				return;

			int caretOffset = editorPane.getCaretPosition();
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

	void onChange(Runnable highlight) {
		document.addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(highlight);
			}

			@Override public void removeUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(highlight);
			}

			@Override public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	void setText(String text) {
		try {
			document.remove(0, document.getLength());
			document.insertString(0, text, null);
		}
		catch (BadLocationException e) {
			throw new ProgrammersFault(e);
		}
	}

	void applyHighlighting(Collection<StyledChunk> styledChunks) {
		for (StyledChunk it : styledChunks)
			document.setCharacterAttributes(it.offset, it.length, it.attributeSet, true);
	}

	/** Returns immutable copy of a document. */
	DocumentSnapshot getDocumentSnapshot() {
		String text;
		try {
			text = document.getText(0, document.getLength());
		}
		catch (BadLocationException e) {
			throw new ProgrammersFault(e);
		}

		Element defaultRootElement = document.getDefaultRootElement();
		int lineCount = defaultRootElement.getElementCount();
		int[] lineStartPositions = new int[lineCount];
		for (int i = 0; i < lineCount; i++)
			lineStartPositions[i] = defaultRootElement.getElement(i).getStartOffset();

		return new DocumentSnapshot(text, lineStartPositions);
	}

	public void setCaretPositionListener(BiConsumer<Integer, Integer> caretPositionListener) {
		this.caretPositionListener = caretPositionListener;
	}
}
