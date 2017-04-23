package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.Program;
import com.xseagullx.jetlang.TokenInformationHolder;
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
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StackMachineCompiler extends Compiler {
	@Override protected Program doParse(JetLangParser.ProgramContext program) {
		List<Statement> statements = program.stmt().stream().map(it -> {
			if (it instanceof JetLangParser.DeclarationContext) {
				JetLangParser.DeclarationContext declarationContext = (JetLangParser.DeclarationContext)it;
				VariableDeclaration variableDeclaration = new VariableDeclaration(declarationContext.identifier().getText(), parse(declarationContext.expr()));
				return addMetadata(variableDeclaration, it);
			}
			else if (it instanceof JetLangParser.OutExprContext) {
				JetLangParser.OutExprContext outExprContext = (JetLangParser.OutExprContext)it;
				return addMetadata(new OutStatement(parse(outExprContext.expr())), it);
			}
			else if (it instanceof JetLangParser.PrintExprContext) {
				JetLangParser.PrintExprContext printExprContext = (JetLangParser.PrintExprContext)it;
				String text = printExprContext.STRING().getText();
				text = "".equals(text) ? "" : text.substring(1, text.length() - 1);
				return new PrintStatement(text);
			}

			return addMetadata(new InvalidStatement(it.getClass(), it.getText(), it), it);
		}).collect(Collectors.toList());

		return new StackBasedProgram(statements);
	}

	private Expression parse(JetLangParser.ExprContext expr) {
		if (expr instanceof JetLangParser.NumberExprContext) {
			return parse((JetLangParser.NumberExprContext)expr);
		}
		else if (expr instanceof JetLangParser.IdentifierExprContext)
			return addMetadata(new VariableExpression(expr.getText()), expr);
		else if (expr instanceof JetLangParser.RangeExprContext) {
			return parse((JetLangParser.RangeExprContext)expr);
		}
		else if (expr instanceof JetLangParser.ParenthesisExprContext) {
			return parse(((JetLangParser.ParenthesisExprContext)expr).expr());
		}
		else if (expr instanceof JetLangParser.BinaryOpExprContext) {
			return parse((JetLangParser.BinaryOpExprContext)expr);
		}
		else if (expr instanceof JetLangParser.MapExprContext) {
			return parse((JetLangParser.MapExprContext)expr);
		}
		else if (expr instanceof JetLangParser.ReduceExprContext) {
			return parse((JetLangParser.ReduceExprContext)expr);
		}
		else
			return addMetadata(new InvalidExpression(expr.getClass() + " " + expr), expr);
	}

	private Expression parse(JetLangParser.ReduceExprContext expr) {
		JetLangParser.ReduceContext reduce = expr.reduce();
		List<JetLangParser.ExprContext> exprContexts = reduce.expr();
		List<String> variableNames = reduce.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList());
		LambdaExpression lambda = new LambdaExpression(variableNames, parse(exprContexts.get(2)));
		addMetadata(lambda, expr); // todo lambda's bounds
		ReduceExpression reduceExpression = new ReduceExpression(parse(exprContexts.get(0)), parse(exprContexts.get(1)), lambda);
		return addMetadata(reduceExpression, expr);
	}

	private Expression parse(JetLangParser.MapExprContext expr) {
		List<JetLangParser.ExprContext> exprContexts = expr.map().expr();
		String variable = expr.map().identifier().IDENTIFIER().getText();
		LambdaExpression lambda = new LambdaExpression(Collections.singletonList(variable), parse(exprContexts.get(1)));
		addMetadata(lambda, expr); // todo lambda's bounds
		return addMetadata(new MapExpression(parse(exprContexts.get(0)), lambda), expr);
	}

	private Expression parse(JetLangParser.BinaryOpExprContext expr) {
		List<JetLangParser.ExprContext> exprContexts = expr.expr();
		BinaryExpression.OperationType operationType = null;
		if (expr.PLUS() != null)
			operationType = BinaryExpression.OperationType.PLUS;
		else if (expr.MINUS() != null)
			operationType = BinaryExpression.OperationType.MINUS;
		else if (expr.DIV() != null)
			operationType = BinaryExpression.OperationType.DIV;
		else if (expr.MUL() != null)
			operationType = BinaryExpression.OperationType.MUL;
		else if (expr.POWER() != null)
			operationType = BinaryExpression.OperationType.POW;

		if (operationType == null)
			throw new RuntimeException("Unsupported operation " + expr);

		BinaryExpression binaryExpression = new BinaryExpression(parse(exprContexts.get(0)), parse(exprContexts.get(1)), operationType);
		return addMetadata(binaryExpression, expr);
	}

	private Expression parse(JetLangParser.RangeExprContext expr) {
		List<JetLangParser.ExprContext> exprContexts = expr.range().expr();
		return addMetadata(new RangeExpression(parse(exprContexts.get(0)), parse(exprContexts.get(1))), expr);
	}

	private Expression parse(JetLangParser.NumberExprContext expr) {
		JetLangParser.NumberContext numberExpr = expr.number();
		Number number;
		if (numberExpr.INTEGER() != null)
			number = Integer.valueOf(numberExpr.INTEGER().getText());
		else if (numberExpr.REAL_NUMBER() != null)
			number = Double.valueOf(numberExpr.REAL_NUMBER().getText());
		else
			throw new RuntimeException("Can't create constant from " + numberExpr);

		return addMetadata(new ConstExpression<>(number), expr);
	}

	private <T extends TokenInformationHolder> T addMetadata(T node, ParserRuleContext ctx) {
		node.setTokenInfo(node.getClass().getSimpleName(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
		return node;
	}
}
