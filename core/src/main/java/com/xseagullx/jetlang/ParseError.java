package com.xseagullx.jetlang;

public class ParseError {
	private final int line;
	private final int col;
	private final String message;
	public final int startOffset;
	public final int endOffset;

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
