package com.xseagullx.jetlang.util;

/** Shall lead to an inevitable and cruel death of our application */
@SuppressWarnings("unused") public class FatalException extends RuntimeException {
	FatalException() {
	}

	FatalException(String message) {
		super(message);
	}

	public FatalException(String message, Throwable cause) {
		super(message, cause);
	}

	FatalException(Throwable cause) {
		super(cause);
	}
}
