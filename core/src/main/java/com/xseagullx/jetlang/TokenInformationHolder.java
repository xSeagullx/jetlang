package com.xseagullx.jetlang;

public class TokenInformationHolder {
	private int offset;
	private int length;
	private int line;
	private int column;
	private String tokenName;

	public void setTokenInfo(String tokenName, int line, int column, int offset, int length) {
		this.line = line;
		this.column = column;
		this.tokenName = tokenName;
		this.offset = offset;
		this.length = length;
	}

	@Override public String toString() {
		return tokenName + " " + line + ":" + column + "";
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
}
