package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.TokenInformationHolder;
import com.xseagullx.jetlang.runtime.CSTUtils;
import com.xseagullx.jetlang.runtime.CompilationVisitor;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.ConstExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.Expression;
import com.xseagullx.jetlang.runtime.stack.nodes.InvalidExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.MapExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.OperationType;
import com.xseagullx.jetlang.runtime.stack.nodes.OutStatement;
import com.xseagullx.jetlang.runtime.stack.nodes.PrintStatement;
import com.xseagullx.jetlang.runtime.stack.nodes.RangeExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.ReduceExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;
import com.xseagullx.jetlang.runtime.stack.nodes.UnaryExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableDeclaration;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableExpression;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class StackMachineVisitor extends CompilationVisitor<TokenInformationHolder> {
	@Override public StackBasedProgram visit(JetLangParser.ProgramContext ctx) {
		return new StackBasedProgram(ctx.stmt().stream().map(this::visit).collect(Collectors.toList()));
	}

	@Override public TokenInformationHolder visit(JetLangParser.BinaryOpExprContext ctx) {
		List<JetLangParser.ExprContext> exprContexts = ctx.expr();
		OperationType operationType = CSTUtils.getOperationType(ctx);
		BinaryExpression binaryExpression = new BinaryExpression(visit(exprContexts.get(0)), visit(exprContexts.get(1)), operationType);
		return addMetadata(binaryExpression, ctx);
	}

	@Override public TokenInformationHolder visit(JetLangParser.UnaryOpExpressionContext ctx) {
		Expression binaryExpression = new UnaryExpression(visit(ctx.expr()), CSTUtils.getOperationType(ctx));
		return addMetadata(binaryExpression, ctx);
	}

	@Override public Expression visit(JetLangParser.NumberExprContext ctx) {
		JetLangParser.NumberContext numberExpr = ctx.number();
		Number number = CSTUtils.getNumber(numberExpr);
		if (number != null)
			return addMetadata(new ConstExpression<>(number), ctx);

		errors.add(new ParseError(ctx.start.getLine(), ctx.start.getCharPositionInLine() + 1, ctx.start.getStartIndex(), ctx.stop.getStopIndex() + 1, "NumberFormatException"));
		return addMetadata(new InvalidExpression("NumberFormatException"), ctx);
	}

	@Override public TokenInformationHolder visit(JetLangParser.IdentifierExprContext ctx) {
		return addMetadata(new VariableExpression(ctx.getText()), ctx);
	}

	@Override public RangeExpression visit(JetLangParser.RangeExprContext ctx) {
		List<JetLangParser.ExprContext> exprContexts = ctx.range().expr();
		return addMetadata(new RangeExpression(visit(exprContexts.get(0)), visit(exprContexts.get(1))), ctx);
	}

	@Override public MapExpression visit(JetLangParser.MapExprContext ctx) {
		List<JetLangParser.ExprContext> exprContexts = ctx.map().expr();
		String variable = ctx.map().identifier().IDENTIFIER().getText();
		JetLangParser.ExprContext lambdaBody = exprContexts.get(1);
		LambdaExpression lambda = new LambdaExpression(Collections.singletonList(variable), visit(lambdaBody));
		Token startOfLambda = ctx.map().identifier().IDENTIFIER().getSymbol();
		addMetadata(lambda, startOfLambda, startOfLambda.getStartIndex(), lambdaBody.getStop().getStopIndex());
		return addMetadata(new MapExpression(visit(exprContexts.get(0)), lambda), ctx);
	}

	@Override public ReduceExpression visit(JetLangParser.ReduceExprContext ctx) {
		JetLangParser.ReduceContext reduce = ctx.reduce();
		List<JetLangParser.ExprContext> exprContexts = reduce.expr();
		List<String> variableNames = reduce.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList());
		JetLangParser.ExprContext lambdaBody = exprContexts.get(2);
		LambdaExpression lambda = new LambdaExpression(variableNames, visit(lambdaBody));
		Token startOfLambda = reduce.IDENTIFIER().get(0).getSymbol();
		addMetadata(lambda, startOfLambda, startOfLambda.getStartIndex(), lambdaBody.getStop().getStopIndex());
		ReduceExpression reduceExpression = new ReduceExpression(visit(exprContexts.get(0)), visit(exprContexts.get(1)), lambda);
		return addMetadata(reduceExpression, ctx);
	}

	@Override public VariableDeclaration visit(JetLangParser.DeclarationContext ctx) {
		VariableDeclaration variableDeclaration = new VariableDeclaration(ctx.identifier().getText(), visit(ctx.expr()));
		return addMetadata(variableDeclaration, ctx);
	}

	@Override public OutStatement visit(JetLangParser.OutExprContext ctx) {
		return addMetadata(new OutStatement(visit(ctx.expr())), ctx);
	}

	@Override public PrintStatement visit(JetLangParser.PrintExprContext ctx) {
		String text = CSTUtils.getString(ctx.STRING());
		return addMetadata(new PrintStatement(text), ctx);
	}

	@Override public Statement visit(JetLangParser.StmtContext ctx) {
		return (Statement)super.visit(ctx);
	}

	@Override public Expression visit(JetLangParser.ExprContext ctx) {
		return (Expression)super.visit(ctx);
	}

	// Utility methods

	private <T extends TokenInformationHolder> T addMetadata(T node, ParserRuleContext ctx) {
		return addMetadata(node, ctx.start, ctx.start.getStartIndex(), ctx.stop.getStopIndex());
	}

	private <T extends TokenInformationHolder> T addMetadata(T node, Token token, int offset, int endIndex) {
		node.setTokenInfo(node.getClass().getSimpleName(), token.getLine(), token.getCharPositionInLine() + 1, offset, endIndex - offset);
		return node;
	}
}
