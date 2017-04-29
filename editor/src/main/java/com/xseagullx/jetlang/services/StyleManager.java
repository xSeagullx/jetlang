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
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class StyleManager {
	private static final Logger log = Logger.getLogger(StyleManager.class.getName());

	public final MutableAttributeSet main = new SimpleAttributeSet();
	public final MutableAttributeSet keyword = new SimpleAttributeSet();
	public final MutableAttributeSet number = new SimpleAttributeSet();
	public final MutableAttributeSet string = new SimpleAttributeSet();
	public final MutableAttributeSet error = new SimpleAttributeSet();

	public final Color caretColor;
	public final Color backgroundColor;
	public final Color foregroundColor;

	public static StyleManager create(StylesBean styles) {
		Color backgroundColor = Color.decode(styles.backgroundColor);
		Color foregroundColor = Color.decode(styles.foregroundColor);
		Color caretColor = Color.decode(styles.caretColor);

		return configure(styles, new StyleManager(backgroundColor, foregroundColor, caretColor));
	}

	private static StyleManager configure(StylesBean styles, StyleManager styleManager) {
		initUI(styleManager.backgroundColor);
		styleManager.configureStyles(styles);
		return styleManager;
	}

	protected StyleManager(Color backgroundColor, Color foregroundColor, Color caretColor) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.caretColor = caretColor;
	}

	private static void initUI(Color backgroundColor) {
		UIDefaults uiDefaults = UIManager.getDefaults();
		uiDefaults.put("EditorPane.background", new ColorUIResource(backgroundColor));
		uiDefaults.put("EditorPane.inactiveBackground", new ColorUIResource(backgroundColor));
	}

	private Font getDefaultFont() {
		try {
			InputStream fontStream = this.getClass().getResourceAsStream("/fonts/Inconsolata-LGC.ttf");
			return Font.createFont(Font.TRUETYPE_FONT, fontStream);
		}
		catch (FontFormatException | IOException e) {
			throw new FatalException("Can't find any suitable font", e);
		}
	}

	private void configureStyles(StylesBean styles) {
		configureStyle("main", styles.main);
		configureStyle("number", styles.number);
		configureStyle("string", styles.string);
		configureStyle("keyword", styles.keyword);
		configureStyle("error", styles.error);
	}

	void configureStyle(String name, StyleBean styleBean) {
		MutableAttributeSet attributeSet = getByName(name);
		if (styleBean == null)
			throw new FatalException("No config found for style: " + name);

		ifDefined(name + ":" + "parent", styleBean.parent, it -> attributeSet.addAttributes(getByName(it)));
		ifDefined(name + ":" + "fontFamily", styleBean.fontFamily, it -> StyleConstants.setFontFamily(attributeSet, getFontFamily(it)));
		ifDefined(name + ":" + "foreground", styleBean.foreground, it -> StyleConstants.setForeground(attributeSet, Color.decode(it)));
		ifDefined(name + ":" + "background", styleBean.background, it -> StyleConstants.setBackground(attributeSet, Color.decode(it)));
		ifDefined(name + ":" + "fontSize", styleBean.fontSize, it -> StyleConstants.setFontSize(attributeSet, it));
		ifDefined(name + ":" + "bold", styleBean.bold, it -> StyleConstants.setBold(attributeSet, it));
		ifDefined(name + ":" + "underline", styleBean.underline, it -> StyleConstants.setUnderline(attributeSet, it));
	}

	private String getFontFamily(String it) {
		return "default".equals(it) ? getDefaultFont().getFamily() : it;
	}

	MutableAttributeSet getByName(String name) {
		switch (name) {
		case "main": return main;
		case "number": return number;
		case "keyword": return keyword;
		case "string": return string;
		case "error": return error;
		default: throw new FatalException("Wrong style name requested: " + name);
		}
	}

	private <T> void ifDefined(String propertyName, T t, Consumer<T> consumer) {
		if (t != null) {
			try {
				consumer.accept(t);
			}
			catch (Exception e) {
				log.log(Level.WARNING, "Exception while setting " + propertyName, e);
			}
		}
	}
}
