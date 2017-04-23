package com.xseagullx.jetlang;

import java.io.File;

public class Launcher {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		// We are Java. We are slow and unresponsive. Add splash.
		// https://docs.oracle.com/javase/tutorial/uiswing/misc/splashscreen.html
		File file = null;
		if (args.length > 0) {
			String fileName = args[0];
			file = new File(fileName);
			if (!file.canRead()) {
				System.out.println("Cannot read file: " + file + "\nIgnoring argument.");
				file = null;
			}
		}
		Editor editor = new Editor();
		if (file != null) {
			editor.open(file);
		}
	}
}
