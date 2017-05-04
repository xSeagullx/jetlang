package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.runtime.jvm.JavaBytecodeCompiler;

public class RunTask extends Task<Void> {
	private DocumentSnapshot documentSnapshot;
	private ExecutionContext context;

	RunTask(DocumentSnapshot documentSnapshot, ExecutionContext context) {
		this.documentSnapshot = documentSnapshot;
		this.context = context;
	}

	@Override public Void call() {
		try {
			context.print("Building...");
			CompilationResult compilationResult = new JavaBytecodeCompiler().parse(documentSnapshot.text);
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
