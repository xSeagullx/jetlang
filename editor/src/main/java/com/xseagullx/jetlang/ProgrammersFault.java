package com.xseagullx.jetlang;

/** Placeholder to analize where did I fail. Techhnically shall be never thwown */
@SuppressWarnings("unused")
class ProgrammersFault extends FatalException {
	ProgrammersFault() {
	}

	ProgrammersFault(String message) {
		super(message);
	}

	ProgrammersFault(String message, Throwable cause) {
		super(message, cause);
	}

	ProgrammersFault(Throwable cause) {
		super(cause);
	}
}
