package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangCompiler;
import com.xseagullx.jetlang.ParseError;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RunTask extends Task<CompletableFuture<Void>> implements Supplier<CompletableFuture<Void>> {
	private final JetLangCompiler compiler;
	private DocumentSnapshot documentSnapshot;
	private ExecutionContext context;

	public RunTask(JetLangCompiler compiler, DocumentSnapshot documentSnapshot, ExecutionContext context) {
		this.compiler = compiler;
		this.documentSnapshot = documentSnapshot;
		this.context = context;
	}

	@Override public CompletableFuture<Void> get() {
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
				context.getExecutionOutcome().complete(null);
				context.print("Execution finished.");
			}
		}
		catch (Throwable e) {
			context.print("Execution failed.");
			throw e;
		}
		return context.getExecutionOutcome();
	}

	@Override public String getId() {
		return "runTask";
	}
}
