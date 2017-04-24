package com.xseagullx.jetlang.services;

import javax.swing.text.AttributeSet;

public class StyledChunk {
	public int offset;
	public int length;
	public AttributeSet attributeSet;

	StyledChunk(int offset, int length, AttributeSet error) {
		this.offset = offset;
		this.length = length;
		attributeSet = error;
	}
}
