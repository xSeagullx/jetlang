package com.xseagullx.jetlang;

public class ParseError {
	final int line;
	final int col;
	final int startOffset;
	final int endOffset;
	final String message;

	ParseError(int line, int col, int startOffset, int endOffset, String message) {
		this.line = line;
		this.col = col;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.message = message;
	}

	@Override public String toString() {
		return "line " + line + ":" + col + " " + message;
	}
}
