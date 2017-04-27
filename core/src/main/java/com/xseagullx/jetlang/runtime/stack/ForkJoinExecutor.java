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
import java.util.logging.Logger;
import java.util.stream.Collectors;

class ApplyLambdaTask extends RecursiveAction {
	private static final Logger log = Logger.getLogger(ParallelExecutor.class.getName());

	private final List<Object> source;
	private final List<Object> result;
	private final int threshold;
	private final ExecutionContext parentContext;
	private final LambdaExpression lambda;

	ApplyLambdaTask(List<Object> source, List<Object> result, ExecutionContext parentContext, LambdaExpression lambda, int threshold) {
		this.source = source;
		this.result = result;
		this.parentContext = parentContext;
		this.lambda = lambda;
		this.threshold = threshold;
	}

	@Override protected void compute() {
		if (parentContext.executionOutcome().isDone())
			return;

		int size = source.size();
		if (size != result.size())
			throw new ThisShouldNeverHappenException("ApplyTransformationTask got two lists with different size");

		if (size > threshold) {
			log.info("Splitting collection of size " + size);
			int halfSize = size / 2;
			ForkJoinTask<Void> firstHalf = new ApplyLambdaTask(source.subList(0, halfSize), result.subList(0, halfSize), parentContext, lambda, threshold).fork();
			ForkJoinTask<Void> secondHalf = new ApplyLambdaTask(source.subList(halfSize, size), result.subList(halfSize, size), parentContext, lambda, threshold).fork();
			firstHalf.join();
			secondHalf.join();
		}
		else {
			log.info("Sequential processing of " + size + " elements");
			ExecutionContext context = parentContext.copy();
			for (int i = 0; i < size; i++)
				result.set(i, lambda.apply(context, source.get(i)));
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
		forkJoinPool.invoke(new ApplyLambdaTask(list, result, context, lambda, 100));
		return result;
	}

	@Override public void destroy() {
		forkJoinPool.shutdown();
	}
}
