package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.services.DocumentSnapshot;
import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.services.StyledChunk;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

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
import java.util.Objects;
import java.util.function.BiConsumer;

/** UI component responsible for showing code and applying highlighting results */
public class EditPanel {
	private final StyleManager styleManager;
	private final StyledDocument document;
	private BiConsumer<Integer, Integer> caretPositionListener;

	public EditPanel(StyleManager styleManager) {
		this.styleManager = styleManager;
		document = new DefaultStyledDocument();
	}

	public Component createComponent() {
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

	public void onChange(Runnable highlight) {
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

	public void setText(String text) {
		try {
			document.remove(0, document.getLength());
			document.insertString(0, text, null);
		}
		catch (BadLocationException e) {
			throw new ThisShouldNeverHappenException(e);
		}
	}

	public void applyHighlighting(Collection<StyledChunk> styledChunks) {
		for (StyledChunk it : styledChunks)
			document.setCharacterAttributes(it.offset, it.length, it.attributeSet, true);
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
