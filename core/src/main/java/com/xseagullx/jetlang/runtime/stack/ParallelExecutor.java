package com.xseagullx.jetlang.runtime.stack;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.runtime.stack.nodes.LambdaExpression;

import java.util.List;

public abstract class ParallelExecutor {
	final int threshold;
	final int paralleismLevel = 4;

	ParallelExecutor(int threshold) {
		this.threshold = threshold;
	}

	public abstract List<Object> map(ExecutionContext context, List<Object> list, LambdaExpression lambda);
	public abstract Object reduce(ExecutionContext context, List<Object> list, Object initialValue, LambdaExpression lambda);

	public void destroy() {
	}
}

