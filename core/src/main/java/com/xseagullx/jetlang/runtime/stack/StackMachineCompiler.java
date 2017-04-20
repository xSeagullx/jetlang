package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.Program;
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
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StackMachineCompiler extends Compiler {
	@Override protected Program doParse(JetLangParser.ProgramContext programm) {
		List<Statement> statements = programm.stmt().stream().map(it -> {
			if (it instanceof JetLangParser.DeclarationContext) {
				JetLangParser.DeclarationContext declarationContext = (JetLangParser.DeclarationContext)it;
				return new VariableDeclaration(declarationContext.identifier().getText(), parseExpression(declarationContext.expr()));
			}
			else if (it instanceof JetLangParser.OutExprContext) {
				JetLangParser.OutExprContext outExprContext = (JetLangParser.OutExprContext)it;
				return new OutStatement(parseExpression(outExprContext.expr()));
			}
			else if (it instanceof JetLangParser.PrintExprContext) {
				JetLangParser.PrintExprContext printExprContext = (JetLangParser.PrintExprContext)it;
				String text = printExprContext.STRING().getText();
				text = "".equals(text) ? "" : text.substring(1, text.length() - 1);
				return new PrintStatement(text);
			}

			return new InvalidStatement(it.getClass(), it.getText(), it);
		}).collect(Collectors.toList());

		return new StackBasedProgram(statements);
	}

	private Expression parseExpression(JetLangParser.ExprContext expr) {
		if (expr instanceof JetLangParser.NumberExprContext) {
			JetLangParser.NumberContext numberExpr = ((JetLangParser.NumberExprContext)expr).number();
			Number number;
			if (numberExpr.INTEGER() != null)
				number = Integer.valueOf(numberExpr.INTEGER().getText());
			else if (numberExpr.REAL_NUMBER() != null)
				number = Double.valueOf(numberExpr.REAL_NUMBER().getText());
			else
				throw new RuntimeException("Can't create constant from " + numberExpr);

			return new ConstExpression<>(number);
		}
		else if (expr instanceof JetLangParser.IdentifierExprContext)
			return new VariableExpression(expr.getText());
		else if (expr instanceof JetLangParser.RangeExprContext) {
			JetLangParser.RangeExprContext rangeExprContext = (JetLangParser.RangeExprContext)expr;
			List<JetLangParser.ExprContext> exprContexts = rangeExprContext.range().expr();
			return new RangeExpression(parseExpression(exprContexts.get(0)), parseExpression(exprContexts.get(1)));
		}
		else if (expr instanceof JetLangParser.ParenthesisExprContext) {
			return parseExpression(((JetLangParser.ParenthesisExprContext)expr).expr());
		}
		else if (expr instanceof JetLangParser.BinaryOpExprContext) {
			JetLangParser.BinaryOpExprContext binaryOpContext = (JetLangParser.BinaryOpExprContext)expr;
			List<JetLangParser.ExprContext> exprContexts = binaryOpContext.expr();
			BinaryExpression.OperationType operationType = null;
			if (binaryOpContext.PLUS() != null)
				operationType = BinaryExpression.OperationType.PLUS;
			else if (binaryOpContext.MINUS() != null)
				operationType = BinaryExpression.OperationType.MINUS;
			else if (binaryOpContext.DIV() != null)
				operationType = BinaryExpression.OperationType.DIV;
			else if (binaryOpContext.MUL() != null)
				operationType = BinaryExpression.OperationType.MUL;
			else if (binaryOpContext.POWER() != null)
				operationType = BinaryExpression.OperationType.POW;

			if (operationType == null)
				throw new RuntimeException("Unsupported operation " + binaryOpContext);

			return new BinaryExpression(parseExpression(exprContexts.get(0)), parseExpression(exprContexts.get(1)), operationType);
		}
		else if (expr instanceof JetLangParser.MapExprContext) {
			JetLangParser.MapExprContext mapExpr = (JetLangParser.MapExprContext)expr;
			List<JetLangParser.ExprContext> exprContexts = mapExpr.map().expr();
			String variable = mapExpr.map().identifier().IDENTIFIER().getText();
			LambdaExpression lambda = new LambdaExpression(Collections.singletonList(variable), parseExpression(exprContexts.get(1)));
			return new MapExpression(parseExpression(exprContexts.get(0)), lambda);
		}
		else if (expr instanceof JetLangParser.ReduceExprContext) {
			JetLangParser.ReduceContext reduce = ((JetLangParser.ReduceExprContext)expr).reduce();
			List<JetLangParser.ExprContext> exprContexts = reduce.expr();
			List<String> variableNames = reduce.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList());
			LambdaExpression lambda = new LambdaExpression(variableNames, parseExpression(exprContexts.get(2)));
			return new ReduceExpression(parseExpression(exprContexts.get(0)), parseExpression(exprContexts.get(1)), lambda);
		}
		else
			return new InvalidExpression(expr.getClass() + " " + expr);
	}
}
