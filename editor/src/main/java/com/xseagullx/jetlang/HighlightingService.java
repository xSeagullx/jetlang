package com.xseagullx.jetlang;

import javax.swing.text.AttributeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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

	Collection<StyledChunk> highlight(DocumentSnapshot documentSnapshot) {
		Collection<StyledChunk> results = new ArrayList<>();

		JetLangLexer lexer = Compiler.getJetLangLexer(documentSnapshot.text, null);

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

			results.add(new StyledChunk(it.getStartIndex(), it.getText().length(), style));
		});

		highlightErrors(documentSnapshot, results);

		return results;
	}

	private void highlightErrors(DocumentSnapshot documentSnapshot, Collection<StyledChunk> results) {
		CompilationResult compilationResult = Compiler.getErrors(documentSnapshot.text);
		if (!compilationResult.hasErrors())
			return;

		for (ParseError error : compilationResult.errors)
			results.add(new StyledChunk(error.startOffset, error.endOffset - error.startOffset, styleManager.error));
	}
}
