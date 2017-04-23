package com.xseagullx.jetlang.runtime.stack.nodes;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.JetLangException;

@FunctionalInterface
interface BinaryOperation {
	Number apply(Number left, Number right);
}

public class BinaryExpression implements Expression {
	public enum OperationType {
		PLUS, MINUS, MUL, DIV, POW
	}

	private static final BinaryOperation INT_PLUS = (Number a, Number b) -> a.intValue() + b.intValue();
	private static final BinaryOperation INT_MINUS = (Number a, Number b) -> a.intValue() - b.intValue();
	private static final BinaryOperation INT_MUL = (Number a, Number b) -> a.intValue() * b.intValue();
	private static final BinaryOperation INT_DIV = (Number a, Number b) -> a.intValue() / b.intValue();

	private static final BinaryOperation DOUBLE_PLUS = (Number a, Number b) -> a.doubleValue() + b.doubleValue();
	private static final BinaryOperation DOUBLE_MINUS = (Number a, Number b) -> a.doubleValue() - b.doubleValue();
	private static final BinaryOperation DOUBLE_MUL = (Number a, Number b) -> a.doubleValue() * b.doubleValue();
	private static final BinaryOperation DOUBLE_DIV = (Number a, Number b) -> a.doubleValue() / b.doubleValue();
	private static final BinaryOperation DOUBLE_POW = (Number a, Number b) -> Math.pow(a.doubleValue(), b.doubleValue());

	private final Expression leftExpr;
	private final Expression rightExpr;
	private final OperationType operationType;

	public BinaryExpression(Expression leftExpr, Expression rightExpr, OperationType operationType) {
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
		this.operationType = operationType;
	}

	@Override public Object exec(ExecutionContext context) {
		Object left = leftExpr.exec(context);
		Object right = rightExpr.exec(context);
		if (!(left instanceof Number) || !(right instanceof Number))
			throw new JetLangException("binary op: " + operationType + " cannot be applied to [" + left + ", " + right + "]");

		return applyOperation((Number)left, (Number)right);
	}

	private Number applyOperation(Number a, Number b) {
		boolean hasDouble = a instanceof Double || b instanceof Double;
		BinaryOperation op;
		switch (operationType) {
		case PLUS: op = hasDouble ? DOUBLE_PLUS : INT_PLUS;break;
		case MINUS: op = hasDouble ? DOUBLE_MINUS : INT_MINUS; break;
		case MUL: op = hasDouble ? DOUBLE_MUL : INT_MUL; break;
		case DIV: op = hasDouble ? DOUBLE_DIV : INT_DIV; break;
		case POW: op = DOUBLE_POW; break;
		default:
			throw new JetLangException("Unsupported Operation type: " + operationType);
		}
		return op.apply(a, b);
	}
}
