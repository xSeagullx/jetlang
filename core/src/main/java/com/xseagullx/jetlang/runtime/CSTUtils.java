package com.xseagullx.jetlang.runtime;

import com.xseagullx.jetlang.JetLangLexer;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class CSTUtils {
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

	public static JetLangParser getJetLangParser(JetLangLexer lexer, List<ParseError> errors) {
		JetLangParser parser = new JetLangParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		if (errors != null) {
			parser.addErrorListener(new BaseErrorListener() {
				@Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					Token token = (Token)offendingSymbol;
					int startOffset = token.getStartIndex();
					int endOffset = startOffset + (token.getText() != null ? token.getText().length() : 1);
					errors.add(new ParseError(token.getLine(), token.getCharPositionInLine() + 1, startOffset, endOffset, msg));
				}
			});
		}
		return parser;
	}

	public static BinaryExpression.OperationType getOperationType(JetLangParser.BinaryOpExprContext ctx) {
		BinaryExpression.OperationType operationType = null;
		if (ctx.PLUS() != null)
			operationType = BinaryExpression.OperationType.PLUS;
		else if (ctx.MINUS() != null)
			operationType = BinaryExpression.OperationType.MINUS;
		else if (ctx.DIV() != null)
			operationType = BinaryExpression.OperationType.DIV;
		else if (ctx.MUL() != null)
			operationType = BinaryExpression.OperationType.MUL;
		else if (ctx.POWER() != null)
			operationType = BinaryExpression.OperationType.POW;

		if (operationType == null)
			throw new RuntimeException("Unsupported operation " + ctx);
		return operationType;
	}

	public static Number getNumber(JetLangParser.NumberContext numberExpr) {
		try {
			Number number;
			if (numberExpr.INTEGER() != null)
				number = Integer.valueOf(numberExpr.INTEGER().getText());
			else if (numberExpr.REAL_NUMBER() != null)
				number = Double.valueOf(numberExpr.REAL_NUMBER().getText());
			else
				throw new ThisShouldNeverHappenException("Can't create constant from " + numberExpr);
			return number;
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	public static String getString(TerminalNode stringToken) {
		String text = stringToken.getText();
		text = "".equals(text) ? "" : text.substring(1, text.length() - 1);
		return text;
	}
}
