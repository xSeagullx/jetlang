package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.util.FatalException;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("WeakerAccess")
public class StyleManager {
	public final MutableAttributeSet main;
	public final MutableAttributeSet keyword;
	public final MutableAttributeSet number;
	public final MutableAttributeSet string;
	public final MutableAttributeSet error;
	public final Color caretColor;

	public StyleManager() {
		Font font;
		try {
			InputStream fontStream = new FileInputStream(new File("/Users/seagull/Dev/home/jetlang/editor/src/main/resources/fonts/Inconsolata-LGC.ttf"));
			font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
		}
		catch (FontFormatException | IOException e) {
			throw new FatalException("Can't find any suitable font", e);
		}

		UIDefaults defs = UIManager.getDefaults();
		defs.put("EditorPane.background", new ColorUIResource(Color.decode("#2B2B2B")));
		defs.put("EditorPane.inactiveBackground", new ColorUIResource(Color.decode("#2B2B2B")));

		main = new SimpleAttributeSet();
		StyleConstants.setFontFamily(main, font.getFamily());
		StyleConstants.setForeground(main, Color.decode("#A9B7C6"));
		StyleConstants.setFontSize(main, 14);

		keyword = new SimpleAttributeSet(main);
		StyleConstants.setForeground(keyword, Color.decode("#CC7832"));
		StyleConstants.setBold(keyword, true);

		number = new SimpleAttributeSet(main);
		StyleConstants.setForeground(number, Color.decode("#6897BB"));

		string = new SimpleAttributeSet(main);
		StyleConstants.setForeground(string, Color.decode("#6A8759"));

		error = new SimpleAttributeSet(main);
		StyleConstants.setForeground(error, Color.decode("#BC3F3C"));
		StyleConstants.setUnderline(error, true);

		caretColor = Color.decode("#bbbbbb");
	}
}
