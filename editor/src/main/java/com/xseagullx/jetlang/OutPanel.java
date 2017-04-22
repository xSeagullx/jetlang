package com.xseagullx.jetlang;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import java.awt.Component;
import java.awt.Dimension;

class OutPanel {
	final StyledDocument outDocument;
	private final StyleManager styleManager;

	OutPanel(StyleManager styleManager) {
		this.styleManager = styleManager;
		outDocument = new DefaultStyledDocument();
	}

	Component getComponent() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setCaretColor(styleManager.caretColor);
		editorPane.setEditorKit(new StyledEditorKit());
		editorPane.setDocument(outDocument);
		JScrollPane jScrollPane = new JScrollPane(editorPane);
		jScrollPane.setPreferredSize(new Dimension(1024, 200)); // FIXME externalize
		return jScrollPane;
	}

	void clear() {
		try {
			outDocument.remove(0, outDocument.getLength());
		}
		catch (BadLocationException e) {
			throw new ProgrammersFault(e);
		}
	}

	void print(String text, AttributeSet style) {
		try {
			outDocument.insertString(0, text, style);
		}
		catch (BadLocationException e) {
			throw new ProgrammersFault(e);
		}
	}
}
