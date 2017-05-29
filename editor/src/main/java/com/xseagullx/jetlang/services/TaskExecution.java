package com.xseagullx.jetlang.services;

public class TaskExecution<T> {
	public enum Status {
		SCHEDULED,
		RUNNING,
		SUCCEEDED,
		FAILED,
		CANCELLED,
	}

	private final Task<T> task;
	Status status;

	TaskExecution(Task<T> task) {
		this.task = task;
	}

	/** Return an info-only copy of this object */
	TaskExecution copy() {
		TaskExecution taskExecution = new TaskExecution<>(task);
		taskExecution.status = status;
		return taskExecution;
	}

	public String getName() {
		return task.getClass().getSimpleName();
	}
}
