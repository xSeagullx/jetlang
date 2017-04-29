package com.xseagullx.jetlang.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

class StyleBean {
	Integer fontSize;
	String fontFamily;
	String foreground;
	String background;
	String parent;
	Boolean bold;
	Boolean underline;
}

class StylesBean {
	String backgroundColor = "#2B2B2B";
	String caretColor = "#bbbbbb";
	String foregroundColor = "#A9B7C6";
	StyleBean main;
	StyleBean keyword;
	StyleBean number;
	StyleBean string;
	StyleBean error;
}

public class ConfigService {
	private static final Logger logger = Logger.getLogger(ConfigService.class.getName());

	public final ConfigBean config;

	private ConfigService(ConfigBean config) {
		this.config = config;
	}

	public static ConfigService create(File file) {
		try {
			return new ConfigService(parseConfig(new FileReader(file)));
		}
		catch (IOException e) {
			logger.log(Level.WARNING, "Error loading `config.json` file. Current dir: " + new File(".").getAbsolutePath(), e);
			return new ConfigService(parseConfig(new InputStreamReader(ConfigService.class.getResourceAsStream("/config.json"))));
		}
	}

	static ConfigBean parseConfig(Reader reader) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(reader, ConfigBean.class);
	}
}
