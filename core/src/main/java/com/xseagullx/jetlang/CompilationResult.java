package com.xseagullx.jetlang;

import java.util.List;

public class CompilationResult {
	public Program program;
	public List<ParseError> errors;

	public CompilationResult(Program program) {
		this.program = program;
	}

	public CompilationResult(List<ParseError> errors) {
		this.errors = errors;
	}

	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
}
