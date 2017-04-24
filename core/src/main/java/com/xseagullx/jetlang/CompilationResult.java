package com.xseagullx.jetlang;

import java.util.List;

public class CompilationResult {
	public Program program;
	public List<ParseError> errors;

	CompilationResult(Program program) {
		this.program = program;
	}

	CompilationResult(List<ParseError> errors) {
		this.errors = errors;
	}

	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
}
