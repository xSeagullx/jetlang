package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangCompiler;
import com.xseagullx.jetlang.runtime.jvm.JavaBytecodeCompiler;
import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;

public class RunService {
	private final TaskManager taskManager;

	public RunService(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void execute(DocumentSnapshot documentSnapshot, ExecutionContext context, boolean useByteCodeCompiler) {
		JetLangCompiler compiler = useByteCodeCompiler ? new JavaBytecodeCompiler() : new StackMachineCompiler();
		RunTask runTask = new RunTask(compiler, documentSnapshot, context);
		taskManager.run(runTask);
	}
}
