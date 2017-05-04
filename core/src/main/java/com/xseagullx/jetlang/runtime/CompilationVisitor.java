package com.xseagullx.jetlang.runtime;

import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.Program;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

import java.util.ArrayList;
import java.util.List;

public abstract class CompilationVisitor<T> {
	protected List<ParseError> errors = new ArrayList<>();

	public abstract Program visit(JetLangParser.ProgramContext ctx);

	// Expressions
	public T visit(JetLangParser.ExprContext expr) {
		if (expr instanceof JetLangParser.BinaryOpExprContext)
			return visit((JetLangParser.BinaryOpExprContext)expr);
		else if (expr instanceof JetLangParser.NumberExprContext)
			return visit((JetLangParser.NumberExprContext)expr);
		else if (expr instanceof JetLangParser.IdentifierExprContext)
			return visit((JetLangParser.IdentifierExprContext)expr);
		else if (expr instanceof JetLangParser.RangeExprContext)
			return visit((JetLangParser.RangeExprContext)expr);
		else if (expr instanceof JetLangParser.ParenthesisExprContext)
			return visit((JetLangParser.ParenthesisExprContext)expr);
		else if (expr instanceof JetLangParser.MapExprContext)
			return visit((JetLangParser.MapExprContext)expr);
		else if (expr instanceof JetLangParser.ReduceExprContext)
			return visit((JetLangParser.ReduceExprContext)expr);
		throw new ThisShouldNeverHappenException("Invalid expression: " + expr);
	}

	public T visit(JetLangParser.ParenthesisExprContext ctx) {
		return visit(ctx.expr());
	}

	public abstract T visit(JetLangParser.BinaryOpExprContext ctx);
	public abstract T visit(JetLangParser.NumberExprContext ctx);
	public abstract T visit(JetLangParser.IdentifierExprContext ctx);
	public abstract T visit(JetLangParser.RangeExprContext ctx);
	public abstract T visit(JetLangParser.MapExprContext ctx);
	public abstract T visit(JetLangParser.ReduceExprContext ctx);

	// Statements
	public T visit(JetLangParser.StmtContext stmt) {
		if (stmt instanceof JetLangParser.DeclarationContext)
			return visit((JetLangParser.DeclarationContext)stmt);
		else if (stmt instanceof JetLangParser.OutExprContext)
			return visit((JetLangParser.OutExprContext)stmt);
		else if (stmt instanceof JetLangParser.PrintExprContext)
			return visit((JetLangParser.PrintExprContext)stmt);
		throw new ThisShouldNeverHappenException("Invalid statement: " + stmt);
	}

	public abstract T visit(JetLangParser.DeclarationContext ctx);
	public abstract T visit(JetLangParser.OutExprContext ctx);
	public abstract T visit(JetLangParser.PrintExprContext ctx);

	public List<ParseError> getErrors() {
		return errors;
	}
}
