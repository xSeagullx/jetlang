package com.xseagullx.jetlang.services;

public class AlreadyRunningException extends RuntimeException {
	public final Task task;

	AlreadyRunningException(Task task) {
		this.task = task;
	}
}
