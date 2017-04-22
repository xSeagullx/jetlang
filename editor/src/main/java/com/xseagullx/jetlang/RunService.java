package com.xseagullx.jetlang;

class RunService {
	void execute(DocumentSnapshot documentSnapshot, ExecutionContext context) {
		RunTask runTask = new RunTask(documentSnapshot);
		runTask.context = context;
		runTask.run(); // TODO in separate thread
	}
}
