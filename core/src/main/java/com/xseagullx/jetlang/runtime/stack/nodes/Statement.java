package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

public interface Statement {
	void exec(ExecutionContext context);
}
