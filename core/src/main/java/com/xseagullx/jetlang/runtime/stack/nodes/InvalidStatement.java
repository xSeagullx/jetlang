package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangParser;
import org.antlr.v4.runtime.ParserRuleContext;

public class InvalidStatement extends Statement {
	public InvalidStatement(Class<? extends JetLangParser.StmtContext> aClass, String text, ParserRuleContext context) {
	}

	@Override public void exec(ExecutionContext context) {
		throw new RuntimeException("Unknown");
	}
}
