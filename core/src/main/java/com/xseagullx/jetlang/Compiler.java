package com.xseagullx.jetlang;

import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public abstract class Compiler {
	public CompilationResult parse(String text) {
		List<ParseError> errors = new ArrayList<>();
		JetLangParser.ProgramContext programCtx = getJetLangParser(getJetLangLexer(text, errors), errors).program();
		if (errors.isEmpty()) {
			return new CompilationResult(doParse(programCtx));
		}
		else
			return new CompilationResult(errors);
	}

	protected abstract Program doParse(JetLangParser.ProgramContext programm);

	public static JetLangLexer getJetLangLexer(String text, List<ParseError> errors) {
		JetLangLexer lexer;
		try {
			// CharStreams.fromString is bugged: https://github.com/antlr/antlr4/issues/1834
			lexer = new JetLangLexer(CharStreams.fromReader(new StringReader(text)));
			lexer.removeErrorListeners(); // remove default error listeners, that print info to stdErr.
			if (errors != null) {
				lexer.addErrorListener(new BaseErrorListener() {
					@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
						int charIndex = ((JetLangLexer)recognizer).getCharIndex();
						errors.add(new ParseError(line, charPositionInLine, charIndex,charIndex + 1, msg));
					}
				});
			}
		}
		catch (IOException e) {
			throw new ThisShouldNeverHappenException(e);
		}
		return lexer;
	}

	private static JetLangParser getJetLangParser(JetLangLexer lexer, List<ParseError> errors) {
		JetLangParser parser = new JetLangParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		if (errors != null) {
			parser.addErrorListener(new BaseErrorListener() {
				@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					Token token = (Token)offendingSymbol;
					int startOffset = token.getStartIndex();
					int endOffset = startOffset + (token.getText() != null ? token.getText().length() : 1);
					errors.add(new ParseError(token.getLine(), token.getCharPositionInLine(), startOffset, endOffset, msg));
				}
			});
		}
		return parser;
	}

	public static CompilationResult getErrors(String text) {
		List<ParseError> errors = new ArrayList<>();
		getJetLangParser(getJetLangLexer(text, errors), errors).program();
		return new CompilationResult(errors);
	}
}
