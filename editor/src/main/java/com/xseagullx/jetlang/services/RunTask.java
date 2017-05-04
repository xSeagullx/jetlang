package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangCompiler;
import com.xseagullx.jetlang.ParseError;

public class RunTask extends Task<Void> {
	private final JetLangCompiler compiler;
	private DocumentSnapshot documentSnapshot;
	private ExecutionContext context;

	RunTask(JetLangCompiler compiler, DocumentSnapshot documentSnapshot, ExecutionContext context) {
		this.compiler = compiler;
		this.documentSnapshot = documentSnapshot;
		this.context = context;
	}

	@Override public Void call() {
		try {
			context.print("Building...");
			CompilationResult compilationResult = compiler.parse(documentSnapshot.text);
			if (compilationResult.hasErrors()) {
				for (ParseError it : compilationResult.errors)
					context.error(it.toString());
			}
			else {
				context.print("Running...");
				compilationResult.program.execute(context);
				context.print("Execution finished.");
			}
		}
		catch (Throwable e) {
			context.print("Execution failed.");
		}
		return null;
	}

	@Override public String getId() {
		return "runTask";
	}
}
