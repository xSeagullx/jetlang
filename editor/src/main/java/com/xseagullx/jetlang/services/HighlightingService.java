package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangLexer;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;
import org.antlr.v4.runtime.Token;

import javax.swing.text.AttributeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HighlightingService {
	private final StyleManager styleManager;

	private Collection<Integer> KEYWORDS = Arrays.asList( // hashSet has more overhead. O(N) here is nothing.
		JetLangLexer.KW_VAR,
		JetLangLexer.KW_MAP,
		JetLangLexer.KW_REDUCE,
		JetLangLexer.KW_OUT,
		JetLangLexer.KW_PRINT
	);

	public HighlightingService(StyleManager styleManager) {
		this.styleManager = styleManager;
	}

	HighlightTask.HighlightingResults highlight(DocumentSnapshot documentSnapshot) {
		Collection<StyledChunk> results = new ArrayList<>();

		JetLangLexer lexer = Compiler.getJetLangLexer(documentSnapshot.text, null);

		for (Token it : lexer.getAllTokens()) {
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
		}

		List<ParseError> parseErrors = highlightErrors(documentSnapshot, results);

		return new HighlightTask.HighlightingResults(parseErrors, results);
	}

	private List<ParseError> highlightErrors(DocumentSnapshot documentSnapshot, Collection<StyledChunk> results) {
		CompilationResult compilationResult = new StackMachineCompiler().parse(documentSnapshot.text);
		if (!compilationResult.hasErrors())
			return null;

		for (ParseError error : compilationResult.errors)
			results.add(new StyledChunk(error.startOffset, error.endOffset - error.startOffset, styleManager.error));
		return compilationResult.errors;
	}
}

