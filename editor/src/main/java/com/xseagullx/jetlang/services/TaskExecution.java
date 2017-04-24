package com.xseagullx.jetlang.services;

import java.util.concurrent.CompletableFuture;

public class TaskExecution<T> {
	public enum Status {
		SCHEDULED,
		RUNNING,
		SUCCEEDED,
		FAILED,
		CANCELLED,
	}

	private final Task<T> task;
	public Status status;
	private CompletableFuture<T> future;

	TaskExecution(Task<T> task) {
		this.task = task;
	}

	TaskExecution<T> start() {
		future = CompletableFuture.supplyAsync(() -> {
			try {
				return task.call();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		return this;
	}

	/** Return an info-only copy of this object */
	public TaskExecution copy() {
		TaskExecution taskExecution = new TaskExecution<>(task);
		taskExecution.status = status;
		return taskExecution;
	}

	public CompletableFuture<T> getFuture() {
		return future;
	}

	public String getName() {
		return task.getClass().getSimpleName();
	}
}
