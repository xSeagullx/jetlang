package com.xseagullx.jetlang.runtime;

import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;

public class CSTUtils {
	public static BinaryExpression.OperationType getOperationType(JetLangParser.BinaryOpExprContext ctx) {
		BinaryExpression.OperationType operationType = null;
		if (ctx.PLUS() != null)
			operationType = BinaryExpression.OperationType.PLUS;
		else if (ctx.MINUS() != null)
			operationType = BinaryExpression.OperationType.MINUS;
		else if (ctx.DIV() != null)
			operationType = BinaryExpression.OperationType.DIV;
		else if (ctx.MUL() != null)
			operationType = BinaryExpression.OperationType.MUL;
		else if (ctx.POWER() != null)
			operationType = BinaryExpression.OperationType.POW;

		if (operationType == null)
			throw new RuntimeException("Unsupported operation " + ctx);
		return operationType;
	}

	public static Number getNumber(JetLangParser.NumberContext numberExpr) {
		try {
			Number number;
			if (numberExpr.INTEGER() != null)
				number = Integer.valueOf(numberExpr.INTEGER().getText());
			else if (numberExpr.REAL_NUMBER() != null)
				number = Double.valueOf(numberExpr.REAL_NUMBER().getText());
			else
				throw new ThisShouldNeverHappenException("Can't create constant from " + numberExpr);
			return number;
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
}
