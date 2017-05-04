package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.CSTUtils;
import com.xseagullx.jetlang.runtime.CompilationVisitor;

import java.util.ArrayList;
import java.util.List;

public abstract class Compiler {
	public CompilationResult parse(String text) {
		List<ParseError> errors = new ArrayList<>();
		JetLangParser.ProgramContext programCtx = CSTUtils.getJetLangParser(CSTUtils.getJetLangLexer(text, errors), errors).program();
		if (errors.isEmpty()) {
			return doParse(programCtx);
		}
		else
			return new CompilationResult(errors);
	}

	private CompilationResult doParse(JetLangParser.ProgramContext program) {
		CompilationVisitor<?> visitor = getVisitor();
		Program stackProgram = visitor.visit(program);
		return visitor.getErrors().isEmpty() ? new CompilationResult(stackProgram) : new CompilationResult(visitor.getErrors());
	}

	protected abstract CompilationVisitor getVisitor();
}
