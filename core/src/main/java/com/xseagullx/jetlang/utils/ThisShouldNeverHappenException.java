package com.xseagullx.jetlang.utils;

public class ThisShouldNeverHappenException extends RuntimeException {
	public ThisShouldNeverHappenException(Throwable e) {
		super(e);
	}
}
