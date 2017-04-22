package com.xseagullx.jetlang;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class Styles {
	final Style main;
	final Style keyword;
	final Style number;
	final Style string;
	final Style error;

	Styles(DefaultStyledDocument document) throws IOException, FontFormatException {
		InputStream fontStream = new FileInputStream(new File("/Users/seagull/Dev/home/jetlang/editor/src/main/resources/fonts/Inconsolata-LGC.ttf"));
		Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);

		UIDefaults defs = UIManager.getDefaults();
		defs.put("EditorPane.background", new ColorUIResource(Color.decode("#2B2B2B")));
		defs.put("EditorPane.inactiveBackground", new ColorUIResource(Color.decode("#2B2B2B")));

		main = document.addStyle("main", null);
		StyleConstants.setFontFamily(main, font.getFamily());
		StyleConstants.setForeground(main, Color.decode("#A9B7C6"));
		StyleConstants.setFontSize(main, 14);

		keyword = document.addStyle("keyword", main);
		StyleConstants.setForeground(keyword, Color.decode("#CC7832"));
		StyleConstants.setBold(keyword, true);

		number = document.addStyle("number", main);
		StyleConstants.setForeground(number, Color.decode("#6897BB"));

		string = document.addStyle("string", main);
		StyleConstants.setForeground(string, Color.decode("#6A8759"));

		error = document.addStyle("error", main);
		StyleConstants.setForeground(error, Color.decode("#BC3F3C"));
		StyleConstants.setUnderline(error, true);
	}
}
