package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.TokenInformationHolder;
import com.xseagullx.jetlang.runtime.CSTUtils;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.ConstExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.InvalidExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.InvalidStatement;
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.MapExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.OutStatement;
import com.xseagullx.jetlang.runtime.stack.nodes.PrintStatement;
import com.xseagullx.jetlang.runtime.stack.nodes.RangeExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.ReduceExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableDeclaration;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableExpression;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StackMachineCompiler extends Compiler {
	@Override protected CompilationResult doParse(JetLangParser.ProgramContext program) {
		List<ParseError> errors = new ArrayList<>();
		List<Statement> statements = program.stmt().stream().map(it -> {
			if (it instanceof JetLangParser.DeclarationContext) {
				JetLangParser.DeclarationContext declarationContext = (JetLangParser.DeclarationContext)it;
				VariableDeclaration variableDeclaration = new VariableDeclaration(declarationContext.identifier().getText(), parse(declarationContext.expr(), errors));
				return addMetadata(variableDeclaration, it);
			}
			else if (it instanceof JetLangParser.OutExprContext) {
				JetLangParser.OutExprContext outExprContext = (JetLangParser.OutExprContext)it;
				return addMetadata(new OutStatement(parse(outExprContext.expr(), errors)), it);
			}
			else if (it instanceof JetLangParser.PrintExprContext) {
				JetLangParser.PrintExprContext printExprContext = (JetLangParser.PrintExprContext)it;
				String text = printExprContext.STRING().getText();
				text = "".equals(text) ? "" : text.substring(1, text.length() - 1);
				return new PrintStatement(text);
			}

			return addMetadata(new InvalidStatement(it.getClass(), it.getText(), it), it);
		}).collect(Collectors.toList());

		return errors.isEmpty() ? new CompilationResult(new StackBasedProgram(statements)) : new CompilationResult(errors);
	}

	private Expression parse(JetLangParser.ExprContext expr, List<ParseError> errors) {
		if (expr instanceof JetLangParser.NumberExprContext) {
			return parse((JetLangParser.NumberExprContext)expr, errors);
		}
		else if (expr instanceof JetLangParser.IdentifierExprContext)
			return addMetadata(new VariableExpression(expr.getText()), expr);
		else if (expr instanceof JetLangParser.RangeExprContext) {
			return parse((JetLangParser.RangeExprContext)expr, errors);
		}
		else if (expr instanceof JetLangParser.ParenthesisExprContext) {
			return parse(((JetLangParser.ParenthesisExprContext)expr).expr(), errors);
		}
		else if (expr instanceof JetLangParser.BinaryOpExprContext) {
			return parse((JetLangParser.BinaryOpExprContext)expr, errors);
		}
		else if (expr instanceof JetLangParser.MapExprContext) {
			return parse((JetLangParser.MapExprContext)expr, errors);
		}
		else if (expr instanceof JetLangParser.ReduceExprContext) {
			return parse((JetLangParser.ReduceExprContext)expr, errors);
		}
		else
			return addMetadata(new InvalidExpression(expr.getClass() + " " + expr), expr);
	}

	private Expression parse(JetLangParser.ReduceExprContext expr, List<ParseError> errors) {
		JetLangParser.ReduceContext reduce = expr.reduce();
		List<JetLangParser.ExprContext> exprContexts = reduce.expr();
		List<String> variableNames = reduce.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList());
		LambdaExpression lambda = new LambdaExpression(variableNames, parse(exprContexts.get(2), errors));
		Token startOfLambda = reduce.IDENTIFIER().get(0).getSymbol();
		addMetadata(lambda, startOfLambda);
		ReduceExpression reduceExpression = new ReduceExpression(parse(exprContexts.get(0), errors), parse(exprContexts.get(1), errors), lambda);
		return addMetadata(reduceExpression, expr);
	}

	private Expression parse(JetLangParser.MapExprContext expr, List<ParseError> errors) {
		List<JetLangParser.ExprContext> exprContexts = expr.map().expr();
		String variable = expr.map().identifier().IDENTIFIER().getText();
		LambdaExpression lambda = new LambdaExpression(Collections.singletonList(variable), parse(exprContexts.get(1), errors));
		Token startOfLambda = expr.map().identifier().IDENTIFIER().getSymbol();
		addMetadata(lambda, startOfLambda);
		return addMetadata(new MapExpression(parse(exprContexts.get(0), errors), lambda), expr);
	}

	private Expression parse(JetLangParser.BinaryOpExprContext expr, List<ParseError> errors) {
		List<JetLangParser.ExprContext> exprContexts = expr.expr();
		BinaryExpression.OperationType operationType = CSTUtils.getOperationType(expr);
		BinaryExpression binaryExpression = new BinaryExpression(parse(exprContexts.get(0), errors), parse(exprContexts.get(1), errors), operationType);
		return addMetadata(binaryExpression, expr);
	}

	private Expression parse(JetLangParser.RangeExprContext expr, List<ParseError> errors) {
		List<JetLangParser.ExprContext> exprContexts = expr.range().expr();
		return addMetadata(new RangeExpression(parse(exprContexts.get(0), errors), parse(exprContexts.get(1), errors)), expr);
	}

	private Expression parse(JetLangParser.NumberExprContext expr, List<ParseError> errors) {
		JetLangParser.NumberContext numberExpr = expr.number();
		Number number = CSTUtils.getNumber(numberExpr);
		if (number != null)
			return addMetadata(new ConstExpression<>(number), expr);

		errors.add(new ParseError(expr.start.getLine(), expr.start.getCharPositionInLine() + 1, expr.start.getStartIndex(), expr.stop.getStopIndex() + 1, "NumberFormatException"));
		return addMetadata(new InvalidExpression("NumberFormatException"), expr);
	}

	private <T extends TokenInformationHolder> T addMetadata(T node, ParserRuleContext ctx) {
		return addMetadata(node, ctx.start);
	}

	private <T extends TokenInformationHolder> T addMetadata(T node, Token token) {
		node.setTokenInfo(node.getClass().getSimpleName(), token.getLine(), token.getCharPositionInLine() + 1);
		return node;
	}
}
