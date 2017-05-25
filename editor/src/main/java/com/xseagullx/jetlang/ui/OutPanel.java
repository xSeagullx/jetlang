package com.xseagullx.jetlang.ui;

import com.xseagullx.jetlang.services.StyleManager;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.Component;
import java.awt.Dimension;

/** UI component that is responsible for showing programm output and compilation results */
public class OutPanel {
	private final StyledDocument document;
	private final StyleManager styleManager;
	private final Dimension preferredSize;

	public OutPanel(StyleManager styleManager, Dimension preferredSize) {
		this.styleManager = styleManager;
		this.preferredSize = preferredSize;
		document = new DefaultStyledDocument();
	}

	public Component getComponent() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setCaretColor(styleManager.caretColor);
		editorPane.setEditorKit(new NoWrapsEditorKit());
		editorPane.setDocument(document);
		JScrollPane jScrollPane = new JScrollPane(editorPane);
		jScrollPane.setPreferredSize(preferredSize);
		return jScrollPane;
	}

	public void clear() {
		try {
			document.remove(0, document.getLength());
		}
		catch (BadLocationException e) {
			throw new ThisShouldNeverHappenException(e);
		}
	}

	public void print(String text, AttributeSet style) {
		try {
			document.insertString(document.getLength(), text, style);
		}
		catch (BadLocationException e) {
			throw new ThisShouldNeverHappenException(e);
		}
	}
}
