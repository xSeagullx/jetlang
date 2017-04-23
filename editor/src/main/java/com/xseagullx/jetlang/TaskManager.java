package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

interface Task extends Runnable {
}

class HighlightTask implements Task {
	int startOffset;
	int endOffset;
	String text;

	@Override public void run() {

	}
}

class RunTask implements Task {
	DocumentSnapshot documentSnapshot;
	ExecutionContext context;

	RunTask(DocumentSnapshot documentSnapshot) {
		this.documentSnapshot = documentSnapshot;
	}

	@Override public void run() {
		new StackMachineCompiler().parse(documentSnapshot.text).execute(context);
	}
}

class TaskExecution {
	enum Status {
		SCHEDULED,
		RUNNING,
		SUCCEEDED,
		FAILED,
		CANCELLED,
	}

	private final Task task;
	public Status status;
	private CompletableFuture<Void> future;
	LocalDateTime startTime = LocalDateTime.now();

	TaskExecution(Task task) {
		this.task = task;
	}

	CompletableFuture<Void> start() {
		future = CompletableFuture.runAsync(task);
		return future;
	}

	/** Return an info-only copy of this object */
	TaskExecution copy() {
		TaskExecution taskExecution = new TaskExecution(task);
		taskExecution.startTime = startTime;
		taskExecution.status = status;
		return taskExecution;
	}

	String getName() {
		return task.getClass().getSimpleName();
	}
}

class AlreadyRunningException extends RuntimeException {
	public final Task task;

	AlreadyRunningException(Task task) {
		this.task = task;
	}
}

public class TaskManager {
	private static final Logger log = Logger.getLogger(TaskManager.class.getName());

	private final ConcurrentHashMap<Class, TaskExecution> tasks = new ConcurrentHashMap<>();
	private final List<Consumer<TaskExecution>> listeners = new ArrayList<>();

	/**
	 * Runs the task. ATM only one instance of same kind task can be executed simultaneously.
	 * If task of this type is already running, @{@link AlreadyRunningException} will be thrown.
	 */
	void run(Task task) {
		Class<? extends Task> taskClass = task.getClass();
		TaskExecution taskExecution = new TaskExecution(task);
		synchronized (tasks) {
			if (tasks.containsKey(taskClass))
				throw new AlreadyRunningException(task);
			tasks.put(taskClass, taskExecution);
			taskExecution.status = TaskExecution.Status.SCHEDULED;
			notifyListeners(taskExecution);
			log.info("Scheduling task: " + task);
		}
		taskExecution.start().thenRun(() -> {
			log.info("Removing task: " + task);
			boolean removed = tasks.remove(taskClass, taskExecution);
			taskExecution.status = TaskExecution.Status.SUCCEEDED;
			notifyListeners(taskExecution);
			log.info("Removing task: " + task + " " + (removed ? "Success" : "Failure"));
			}
		);
	}

	Collection<TaskExecution> runningTasks() {
		synchronized (tasks) {
			return tasks.values().stream().map(TaskExecution::copy).collect(Collectors.toList());
		}
	}

	void subscribe(Consumer<TaskExecution> eventListener) {
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
