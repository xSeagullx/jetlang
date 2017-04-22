package com.xseagullx.jetlang;

public class Launcher {
	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		// We are Java. We are slow and unresponsive. Add splash.
		// https://docs.oracle.com/javase/tutorial/uiswing/misc/splashscreen.html
		new Editor().open();
	}
}
