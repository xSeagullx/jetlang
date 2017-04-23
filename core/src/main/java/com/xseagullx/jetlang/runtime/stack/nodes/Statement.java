package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.TokenInformationHolder;

public abstract class Statement extends TokenInformationHolder {
	public abstract void exec(ExecutionContext context);
}
