package com.xseagullx.jetlang.runtime.jvm;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.runtime.jvm.ProgramBase;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableExpression;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

class JVMCompilationContext {
	Map<String, Integer> localVariables = new HashMap<>();
	Queue<Map.Entry<String, ExpressionContext>> lambdas = new ArrayDeque<>();
	MethodVisitor methodVisitor;
}

public class JavaBytecodeCompiler extends Compiler {
	@Override protected CompilationResult doParse(JetLangParser.ProgramContext program) {
		generateClass(program);
		return null;
	}

	byte[] generateClass(JetLangParser.ProgramContext programCtx) {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classWriter.visit(49, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "Program", null, asmClassName(ProgramBase.class), null);
		createInitMethod(classWriter);
		createRunMethod(programCtx, classWriter);
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	private void createRunMethod(JetLangParser.ProgramContext programCtx, ClassWriter classWriter) {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);

		JVMCompilationContext jvm = new JVMCompilationContext();
		jvm.methodVisitor = mv;

		for (JetLangParser.StmtContext stmtContext : programCtx.stmt()) {
			if (stmtContext instanceof JetLangParser.DeclarationContext) {
				JetLangParser.DeclarationContext declarationCtx = (JetLangParser.DeclarationContext)stmtContext;
				String name = declarationCtx.identifier().IDENTIFIER().getText();
				generateExpression(declarationCtx.expr(), jvm);
				mv.visitVarInsn(Opcodes.ASTORE, 1);
				jvm.localVariables.put(name, 1);
			} else if (stmtContext instanceof JetLangParser.OutExprContext) {
				JetLangParser.OutExprContext outExprContext = (JetLangParser.OutExprContext)stmtContext;
				mv.visitVarInsn(Opcodes.ALOAD, 0); // this
				generateExpression(outExprContext.expr(), jvm);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "out", "(Ljava/lang/Object;)V");
			}
		}

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void generateExpression(JetLangParser.BinaryOpExprContext ctx, JVMCompilationContext jvm) {
		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); // this
		generateExpression(ctx.expr(0), jvm);
		generateExpression(ctx.expr(1), jvm);
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
		generateOperationCall(operationType, jvm);
	}

	private void generateOperationCall(BinaryExpression.OperationType operationType, JVMCompilationContext jvm) {
		String name = operationType.name().toLowerCase();
		jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", name, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	}

	private void generateExpression(JetLangParser.ExprContext expr, JVMCompilationContext jvm) {
		if (expr instanceof JetLangParser.BinaryOpExprContext)
			generateExpression((JetLangParser.BinaryOpExprContext)expr, jvm);
		else if (expr instanceof JetLangParser.NumberExprContext)
			generateExpression((JetLangParser.NumberExprContext)expr, jvm);
		else if (expr instanceof JetLangParser.IdentifierExprContext)
			generateExpression((JetLangParser.IdentifierExprContext)expr, jvm);
		else if (expr instanceof JetLangParser.RangeExprContext)
			generateExpression((JetLangParser.RangeExprContext)expr, jvm);
		else if (expr instanceof JetLangParser.ParenthesisExprContext)
			generateExpression(((JetLangParser.ParenthesisExprContext)expr).expr(), jvm);
	}

	private void generateExpression(JetLangParser.IdentifierExprContext expr, JVMCompilationContext jvm) {
		String name = expr.identifier().IDENTIFIER().getText();
		int pos = jvm.localVariables.get(name); // todo check if declared
		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, pos);
	}

	private void generateExpression(JetLangParser.RangeExprContext expr, JVMCompilationContext jvm) {
//		expr.identifier()
	}

	private void generateExpression(JetLangParser.NumberExprContext expr, JVMCompilationContext jvm) {
		JetLangParser.NumberContext numberCtx = expr.number();
		if (numberCtx.INTEGER() != null) {
			Integer value = Integer.valueOf(numberCtx.INTEGER().getText());
			jvm.methodVisitor.visitIntInsn(Opcodes.SIPUSH, value);
			jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, asmClassName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;");
		}
		else if (numberCtx.REAL_NUMBER() != null) {
			Double value = Double.valueOf(numberCtx.REAL_NUMBER().getText());
			jvm.methodVisitor.visitLdcInsn(value);
			jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, asmClassName(Double.class), "valueOf", "(D)Ljava/lang/Double;");
		}
		else {
			throw new ThisShouldNeverHappenException("");
		}
	}

	private void createInitMethod(ClassWriter classWriter) {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
			asmClassName(ProgramBase.class),
			"<init>",
			"()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private String asmClassName(Class clazz) {
		return clazz.getName().replace('.', '/');
	}
}
