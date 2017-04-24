package com.xseagullx.jetlang.services;

import com.xseagullx.jetlang.ExecutionContext;

public class RunService {
	private final TaskManager taskManager;

	public RunService(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void execute(DocumentSnapshot documentSnapshot, ExecutionContext context) {
		RunTask runTask = new RunTask(documentSnapshot, context);
		taskManager.run(runTask);
	}
}
