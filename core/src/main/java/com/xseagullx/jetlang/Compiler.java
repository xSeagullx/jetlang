package com.xseagullx.jetlang;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public abstract class Compiler {
	public Program parse(String text) {
		JetLangLexer lexer = new JetLangLexer(CharStreams.fromString(text));
		JetLangParser.ProgramContext program = new JetLangParser(new CommonTokenStream(lexer)).program();

		return doParse(program);
	}

	protected abstract Program doParse(JetLangParser.ProgramContext programm);
}
