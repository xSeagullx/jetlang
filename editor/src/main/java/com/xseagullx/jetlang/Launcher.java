package com.xseagullx.jetlang;

import com.xseagullx.jetlang.utils.FileUtils;

import java.io.File;
import java.util.Arrays;

public class Launcher {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		// We are Java. We are slow and unresponsive. Add splash.
		// https://docs.oracle.com/javase/tutorial/uiswing/misc/splashscreen.html

		File file = args.length > 0 ? FileUtils.validateFilePath(args[0]) : null;
		Editor editor = new Editor();
		if (file == null)
			System.out.println("Cannot read provided file: " + Arrays.toString(args) + "\nIgnoring argument.");
		else
			editor.open(file);
	}
}
