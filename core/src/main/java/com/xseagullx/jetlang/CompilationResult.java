package com.xseagullx.jetlang;

import java.util.List;

public class CompilationResult {
	Program program;
	List<ParseError> errors;

	CompilationResult(Program program) {
		this.program = program;
	}

	CompilationResult(List<ParseError> errors) {
		this.errors = errors;
	}

	boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
}
