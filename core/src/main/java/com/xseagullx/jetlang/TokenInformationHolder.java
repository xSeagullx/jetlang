package com.xseagullx.jetlang;

public class TokenInformationHolder {
	private int line;
	private int column;
	private String tokenName;

	public void setTokenInfo(String tokenName, int line, int column) {
		this.line = line;
		this.column = column;
		this.tokenName = tokenName;
	}

	@Override public String toString() {
		return tokenName + " " + line + ":" + column + "";
	}
}
