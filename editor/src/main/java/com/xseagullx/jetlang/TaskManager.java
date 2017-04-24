package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

abstract class Task<T> implements Callable<T> {
	abstract String getId();

	@Override public String toString() {
		return getClass() + " (" + getId() + ")";
	}
}

class RunTask extends Task<Void> {
	private DocumentSnapshot documentSnapshot;
	private ExecutionContext context;

	RunTask(DocumentSnapshot documentSnapshot, ExecutionContext context) {
		this.documentSnapshot = documentSnapshot;
		this.context = context;
	}

	@Override public Void call() {
		try {
			context.print("Building...");
			CompilationResult compilationResult = new StackMachineCompiler().parse(documentSnapshot.text);
			if (compilationResult.hasErrors()) {
				for (ParseError it : compilationResult.errors)
					context.error(it.toString());
			}
			else {
				context.print("Running...");
				compilationResult.program.execute(context);
				context.print("Execution finished.");
			}
		}
		catch (Throwable e) {
			context.print("Execution failed.");
		}
		return null;
	}

	@Override public String getId() {
		return "runTask";
	}
}

class TaskExecution<T> {
	enum Status {
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
	TaskExecution copy() {
		TaskExecution taskExecution = new TaskExecution<>(task);
		taskExecution.status = status;
		return taskExecution;
	}

	CompletableFuture<T> getFuture() {
		return future;
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
