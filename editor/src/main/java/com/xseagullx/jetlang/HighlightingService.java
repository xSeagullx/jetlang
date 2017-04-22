package com.xseagullx.jetlang;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import javax.swing.text.AttributeSet;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

class HighlightingService {
	private final StyleManager styleManager;
	private Collection<Integer> KEYWORDS = Arrays.asList( // hashSet has more overhead. O(N) here is nothing.
		JetLangLexer.KW_VAR,
		JetLangLexer.KW_MAP,
		JetLangLexer.KW_REDUCE,
		JetLangLexer.KW_OUT,
		JetLangLexer.KW_PRINT
	);


	HighlightingService(StyleManager styleManager) {
		this.styleManager = styleManager;
	}

	Collection<StyledChunk> highlight(DocumentSnapshot documentSnapshot, Consumer<String> showInOutputPane, Consumer<String> showErrorInOutputPane) {
		Collection<StyledChunk> results = new ArrayList<>();

		JetLangLexer lexer = getJetLangLexer(documentSnapshot);
		lexer.removeErrorListeners(); // remove default listeners, that print error to stderr.

		lexer.addErrorListener(new BaseErrorListener() {
			@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				showErrorInOutputPane.accept(line + ":" + (charPositionInLine + 1) + " ERROR: " + e + "\n");

				int lineOffset = documentSnapshot.lineOffsets[line - 1];
				results.add(new StyledChunk(lineOffset + charPositionInLine, 1, styleManager.error));
			}
		});

		lexer.getAllTokens().forEach((it) -> {
			AttributeSet style;
			if (KEYWORDS.contains(it.getType()))
				style = styleManager.keyword;
			else if (it.getType() == JetLangLexer.STRING)
				style = styleManager.string;
			else if (it.getType() == JetLangLexer.INTEGER || it.getType() == JetLangLexer.REAL_NUMBER)
				style = styleManager.number;
			else
				style = styleManager.main;

			showInOutputPane.accept(it.getStartIndex() + ":" + it.getStopIndex() + " " + JetLangLexer.VOCABULARY.getDisplayName(it.getType()) + " " + it.getText() + "\n");
			results.add(new StyledChunk(it.getStartIndex(), it.getText().length(), style));
		});

		highlightParseErrors(documentSnapshot, results);

		return results;
	}

	private JetLangLexer getJetLangLexer(DocumentSnapshot documentSnapshot) {
		JetLangLexer lexer;
		try {
			lexer = new JetLangLexer(CharStreams.fromReader(new StringReader(documentSnapshot.text)));
		}
		catch (IOException e) {
			throw new ProgrammersFault();
		}
		return lexer;
	}

	private void highlightParseErrors(DocumentSnapshot documentSnapshot, Collection<StyledChunk> results) {
		JetLangLexer lexer =  getJetLangLexer(documentSnapshot);
		JetLangParser parser = new JetLangParser(new CommonTokenStream(lexer));
		lexer.removeErrorListeners();
		parser.removeErrorListeners();

		parser.addErrorListener(new BaseErrorListener() {
			@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				Token token = (Token)offendingSymbol;
				results.add(new StyledChunk(token.getStartIndex(), token.getText() != null ? token.getText().length() : 1, styleManager.error));
			}
		});
		parser.program();
	}
}
