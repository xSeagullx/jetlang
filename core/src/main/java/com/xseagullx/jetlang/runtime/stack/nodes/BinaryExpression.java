package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;

@FunctionalInterface
interface BinaryOperation {
	Number apply(Number left, Number right);
}

public class BinaryExpression implements Expression {
	public enum OperationType {
		PLUS, MINUS, MUL, DIV, POW
	}

	private final Expression leftExpr;
	private final Expression rightExpr;
	private final OperationType operationType;
	private final BinaryOperation op;

	public BinaryExpression(Expression leftExpr, Expression rightExpr, OperationType operationType) {
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
		this.operationType = operationType;

		switch (operationType) {
		case PLUS: op = (Number a, Number b) -> a.doubleValue() + b.doubleValue(); break;
		case MINUS: op = (Number a, Number b) -> a.doubleValue() - b.doubleValue(); break;
		case MUL: op = (Number a, Number b) -> a.doubleValue() * b.doubleValue(); break;
		case DIV: op = (Number a, Number b) -> a.doubleValue() / b.doubleValue(); break;
		case POW: op = (Number a, Number b) -> Math.pow(a.doubleValue(), b.doubleValue()); break;
		default:
			throw new RuntimeException("Unsupported Operation type: " + operationType);
		}
	}

	@Override public Object exec(ExecutionContext context) {
		Object left = leftExpr.exec(context);
		Object right = rightExpr.exec(context);
		if (!(left instanceof Number) || !(right instanceof Number))
			throw new RuntimeException("binary op: " + operationType + " cannot be applied to [" + left + ", " + right + "]");

		return op.apply((Number)left, (Number)right);
	}
}
