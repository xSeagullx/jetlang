package com.xseagullx.jetlang;

/** Shall lead to an inevitable and cruel death of our application */
@SuppressWarnings("unused")
class FatalException extends RuntimeException {
	FatalException() {
	}

	FatalException(String message) {
		super(message);
	}

	FatalException(String message, Throwable cause) {
		super(message, cause);
	}

	FatalException(Throwable cause) {
		super(cause);
	}
}
