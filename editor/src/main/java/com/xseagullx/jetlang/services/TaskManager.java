package com.xseagullx.jetlang.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TaskManager {
	private static final Logger log = Logger.getLogger(TaskManager.class.getName());

	private final ConcurrentHashMap<String, TaskExecution> tasks = new ConcurrentHashMap<>();
	private final List<Consumer<TaskExecution>> listeners = new ArrayList<>();

	/**
	 * Runs the task. ATM only one instance of same kind task can be executed simultaneously.
	 * If task of this type is already running, @{@link AlreadyRunningException} will be thrown.
	 */
	public <T> CompletableFuture<TaskExecution<T>> run(Task<T> task) {
		TaskExecution<T> taskExecution = new TaskExecution<>(task);
		synchronized (tasks) {
			if (tasks.containsKey(task.getId()))
				throw new AlreadyRunningException(task);
			tasks.put(task.getId(), taskExecution);
			taskExecution.status = TaskExecution.Status.SCHEDULED;
			notifyListeners(taskExecution);
			log.info("Scheduling task: " + task);
		}
		return taskExecution.start().getFuture().handle((ignored, error) -> {
				log.info("Removing task: " + task);
				boolean removed = tasks.remove(task.getId(), taskExecution);
				taskExecution.status = error != null ? TaskExecution.Status.FAILED : TaskExecution.Status.SUCCEEDED;
				if (error != null)
					error.printStackTrace();
				notifyListeners(taskExecution);
				log.info("Removing task: " + task + " " + (removed ? "Success" : "Failure"));
				return taskExecution;
			}
		);
	}

	public Collection<TaskExecution> runningTasks() {
		synchronized (tasks) {
			return tasks.values().stream().map(TaskExecution::copy).collect(Collectors.toList());
		}
	}

	public void subscribe(Consumer<TaskExecution> eventListener) {
		synchronized (listeners) {
			listeners.add(eventListener);
		}
	}

	private void notifyListeners(TaskExecution execution) {
		synchronized (listeners) {
			listeners.forEach(it -> it.accept(execution));
		}
	}
}
