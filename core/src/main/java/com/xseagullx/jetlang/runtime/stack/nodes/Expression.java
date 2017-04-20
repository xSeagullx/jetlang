package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public interface Expression {
	Object exec(ExecutionContext context);
}
