package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class ParallelTaskHelper {
	private List<Object> source;
	private final int threshold;
	final ExecutionContext parentContext;
	final LambdaExpression lambda;

	ParallelTaskHelper(ExecutionContext parentContext, List<Object> source, LambdaExpression lambda, int threshold) {
		this.source = source;
		this.threshold = threshold;
		this.parentContext = parentContext;
		this.lambda = lambda;
	}

	private ParallelTaskHelper(ParallelTaskHelper helper) {
		this.threshold = helper.threshold;
		this.lambda = helper.lambda;
		this.parentContext = helper.parentContext;
	}

	ParallelTaskHelper withSource(int from, int to) {
		ParallelTaskHelper newHelper = new ParallelTaskHelper(this);
		newHelper.source = source.subList(from, to);
		return newHelper;
	}

	boolean shallBeStopped() {
		return parentContext.getExecutionOutcome().isDone();
	}

	boolean shouldSplit() {
		return source.size() > threshold;
	}

	List<Object> getSource() {
		return source;
	}
}

class ApplyMapTask extends RecursiveAction {
	private static final Logger log = Logger.getLogger(ApplyMapTask.class.getName());

	private final List<Object> result;
	private final ParallelTaskHelper helper;

	ApplyMapTask(List<Object> result, ParallelTaskHelper helper) {
		this.result = result;
		this.helper = helper;
	}

	@Override protected void compute() {
		if (helper.shallBeStopped())
			return;

		int size = helper.getSource().size();
		if (size != result.size())
			throw new ThisShouldNeverHappenException("ApplyTransformationTask got two lists with different size");

		if (helper.shouldSplit()) {
			log.info("Splitting collection of size " + size);
			int halfSize = size / 2;
			ForkJoinTask<Void> firstHalf = new ApplyMapTask(result.subList(0, halfSize), helper.withSource(0, halfSize)).fork();
			ForkJoinTask<Void> secondHalf = new ApplyMapTask(result.subList(halfSize, size), helper.withSource(halfSize, size)).fork();
			firstHalf.join();
			secondHalf.join();
		}
		else {
			log.info("Mapping sequentially " + size + " elements");
			ExecutionContext context = helper.parentContext.copy();
			for (int i = 0; i < size; i++)
				result.set(i, helper.lambda.apply(context, helper.getSource().get(i)));
		}
	}
}

class ApplyReduceTask extends RecursiveTask<Object> {
	private static final Logger log = Logger.getLogger(ApplyReduceTask.class.getName());

	private final Object initialValue;
	private final ParallelTaskHelper helper;

	ApplyReduceTask(Object initialValue, ParallelTaskHelper helper) {
		this.helper = helper;
		this.initialValue = initialValue;
	}

	@Override protected Object compute() {
		if (helper.shallBeStopped())
			return null;

		int size = helper.getSource().size();
		if (helper.shouldSplit()) {
			log.info(Thread.currentThread().getName() + "Splitting collection of size " + size);
			int halfSize = size / 2;
			ForkJoinTask<Object> firstHalf = new ApplyReduceTask(initialValue, helper.withSource(0, halfSize)).fork();
			ForkJoinTask<Object> secondHalf = new ApplyReduceTask(helper.getSource().get(halfSize), helper.withSource(halfSize + 1, size)).fork();
			Object firstHalfReduce = firstHalf.join();
			Object secondHalfReduce = secondHalf.join();
			log.info(Thread.currentThread().getName() + "Aggregating " + firstHalfReduce + " and " + secondHalfReduce);
			ExecutionContext context = helper.parentContext.copy();
			return helper.lambda.apply(context, firstHalfReduce, secondHalfReduce);
		}
		else {
			log.info("Reducing sequentially " + size + " elements");
			ExecutionContext context = helper.parentContext.copy();
			return helper.getSource().stream().reduce(initialValue, (acc, i) -> helper.lambda.apply(context, acc, i));
		}
	}
}

public class ForkJoinExecutor extends ParallelExecutor {
	private static final Logger log = Logger.getLogger(ParallelExecutor.class.getName());

	private final ForkJoinPool forkJoinPool;

	public ForkJoinExecutor(int threshold) {
		super(threshold);
		forkJoinPool = new ForkJoinPool(paralleismLevel);
	}

	@Override public List<Object> map(ExecutionContext context, List<Object> list, LambdaExpression lambda) {
		if (list.size() <= threshold) {
			log.info("Executing sequentially. Size: " + list.size());
			return list.stream().map(it -> lambda.apply(context, it)).collect(Collectors.toList());
		}

		log.info("Executing in parallel. Size: " + list.size());
		ArrayList<Object> result = new ArrayList<>(Collections.nCopies(list.size(), null));
		forkJoinPool.invoke(new ApplyMapTask(result, new ParallelTaskHelper(context, list, lambda, threshold)));
		return result;
	}

	@Override public Object reduce(ExecutionContext context, List<Object> list, Object initialValue, LambdaExpression lambda) {
		if (list.size() <= threshold) {
			log.info("Executing sequentially. Size: " + list.size());
			return list.stream().reduce(initialValue, (acc, i) -> lambda.apply(context, acc, i));
		}

		log.info("Executing in parallel. Size: " + list.size());
		return forkJoinPool.invoke(new ApplyReduceTask(initialValue, new ParallelTaskHelper(context, list, lambda, threshold)));
	}

	@Override public void destroy() {
		forkJoinPool.shutdown();
	}
}
