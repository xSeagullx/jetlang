package com.xseagullx.jetlang.runtime;

import com.xseagullx.jetlang.JetLangParser;

public abstract class CompilationVisitor {
	public abstract void visit(JetLangParser.ProgramContext ctx);

	// Expressions
	public void visit(JetLangParser.ExprContext expr) {
		if (expr instanceof JetLangParser.BinaryOpExprContext)
			visit((JetLangParser.BinaryOpExprContext)expr);
		else if (expr instanceof JetLangParser.NumberExprContext)
			visit((JetLangParser.NumberExprContext)expr);
		else if (expr instanceof JetLangParser.IdentifierExprContext)
			visit((JetLangParser.IdentifierExprContext)expr);
		else if (expr instanceof JetLangParser.RangeExprContext)
			visit((JetLangParser.RangeExprContext)expr);
		else if (expr instanceof JetLangParser.ParenthesisExprContext)
			visit((JetLangParser.ParenthesisExprContext)expr);
		else if (expr instanceof JetLangParser.MapExprContext)
			visit((JetLangParser.MapExprContext)expr);
		else if (expr instanceof JetLangParser.ReduceExprContext)
			visit((JetLangParser.ReduceExprContext)expr);
	}

	public void visit(JetLangParser.ParenthesisExprContext ctx) {
		visit(ctx.expr());
	}

	public abstract void visit(JetLangParser.BinaryOpExprContext ctx);
	public abstract void visit(JetLangParser.NumberExprContext ctx);
	public abstract void visit(JetLangParser.IdentifierExprContext ctx);
	public abstract void visit(JetLangParser.RangeExprContext ctx);
	public abstract void visit(JetLangParser.MapExprContext ctx);
	public abstract void visit(JetLangParser.ReduceExprContext ctx);

	// Statements
	public void visit(JetLangParser.StmtContext stmt) {
		if (stmt instanceof JetLangParser.DeclarationContext)
			visit((JetLangParser.DeclarationContext)stmt);
		else if (stmt instanceof JetLangParser.OutExprContext)
			visit((JetLangParser.OutExprContext)stmt);
		else if (stmt instanceof JetLangParser.PrintExprContext)
			visit((JetLangParser.PrintExprContext)stmt);
	}

	public abstract void visit(JetLangParser.DeclarationContext ctx);
	public abstract void visit(JetLangParser.OutExprContext ctx);
	public abstract void visit(JetLangParser.PrintExprContext ctx);
}
