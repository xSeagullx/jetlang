package com.xseagullx.jetlang.utils;

public class ThisShouldNeverHappenException extends RuntimeException {
	public ThisShouldNeverHappenException(String message) {
		super(message);
	}

	public ThisShouldNeverHappenException(Throwable e) {
		super(e);
	}
}
