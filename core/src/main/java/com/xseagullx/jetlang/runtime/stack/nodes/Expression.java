package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.TokenInformationHolder;

public abstract class Expression extends TokenInformationHolder {
	public abstract Object exec(ExecutionContext context);
}
