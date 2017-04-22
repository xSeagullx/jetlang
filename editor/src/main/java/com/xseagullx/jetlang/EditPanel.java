package com.xseagullx.jetlang;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
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
	private final StyledDocument inDocument;

	EditPanel(StyleManager styleManager) {
		this.styleManager = styleManager;
		inDocument = new DefaultStyledDocument();
	}

	Component createComponent(JFrame frame) {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setCaretColor(styleManager.caretColor);
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

	void onChange(Runnable highlight) {
		inDocument.addDocumentListener(new DocumentListener() {
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
			inDocument.remove(0, inDocument.getLength());
			inDocument.insertString(0, text, null);
		}
		catch (BadLocationException e) {
			throw new ProgrammersFault(e);
		}
	}

	void applyHighlighting(Collection<StyledChunk> styledChunks) {
		for (StyledChunk it : styledChunks)
			inDocument.setCharacterAttributes(it.offset, it.length, it.attributeSet, true);
	}

	/** Returns immutable copy of a document. */
	DocumentSnapshot getDocumentSnapshot() {
		String text;
		try {
			text = inDocument.getText(0, inDocument.getLength());
		}
		catch (BadLocationException e) {
			throw new ProgrammersFault(e);
		}

		Element defaultRootElement = inDocument.getDefaultRootElement();
		int lineCount = defaultRootElement.getElementCount();
		int[] lineStartPositions = new int[lineCount];
		for (int i = 0; i < lineCount; i++)
			lineStartPositions[i] = defaultRootElement.getElement(i).getStartOffset();

		return new DocumentSnapshot(text, lineStartPositions);
	}
}
