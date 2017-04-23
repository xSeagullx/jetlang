package com.xseagullx.jetlang;

class RunService {
	private final TaskManager taskManager;

	RunService(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	void execute(DocumentSnapshot documentSnapshot, ExecutionContext context) {
		RunTask runTask = new RunTask(documentSnapshot);
		runTask.context = context;
		taskManager.run(runTask);
	}
}
