package com.xseagullx.jetlang;

import javax.swing.text.AttributeSet;

class StyledChunk {
	int offset;
	int length;
	AttributeSet attributeSet;

	StyledChunk(int offset, int length, AttributeSet error) {
		this.offset = offset;
		this.length = length;
		attributeSet = error;
	}
}
