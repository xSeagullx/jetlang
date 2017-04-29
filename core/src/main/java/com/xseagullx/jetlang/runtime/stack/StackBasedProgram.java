package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Program;
import com.xseagullx.jetlang.runtime.stack.nodes.Statement;

import java.util.List;

class StackBasedProgram implements Program {
	private final List<Statement> statements;

	StackBasedProgram(List<Statement> statements) {
		this.statements = statements;
	}

	@Override public void execute() {
		execute(null);
	}

	@Override public void execute(ExecutionContext existingContext) {
		ForkJoinExecutor forkJoinExecutor = null;
		try {
			forkJoinExecutor = new ForkJoinExecutor(100);
			ExecutionContext context = existingContext != null ? existingContext : new SimpleExecutionContext(forkJoinExecutor);
			for (Statement statement : statements)
				context.exec(statement);
		}
		finally {
			if (forkJoinExecutor != null)
				forkJoinExecutor.destroy();
		}
	}
}
