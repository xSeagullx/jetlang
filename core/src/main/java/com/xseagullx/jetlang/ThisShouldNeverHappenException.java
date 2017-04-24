package com.xseagullx.jetlang;

class ThisShouldNeverHappenException extends RuntimeException {
	ThisShouldNeverHappenException(Throwable e) {
		super(e);
	}
}
