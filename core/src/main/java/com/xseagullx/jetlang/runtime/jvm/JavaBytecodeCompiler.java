package com.xseagullx.jetlang.runtime.jvm;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.Sequence;
import com.xseagullx.jetlang.runtime.jvm.ProgramBase;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableExpression;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

class LambdaDefinition {
	List<String> variables;
	JetLangParser.ExprContext expression;
	String name;
}

class JVMCompilationContext {
	Map<String, Integer> localVariables = new HashMap<>();
	Queue<LambdaDefinition> lambdas = new ArrayDeque<>();
	MethodVisitor methodVisitor;
}

public class JavaBytecodeCompiler extends Compiler {
	@Override protected CompilationResult doParse(JetLangParser.ProgramContext program) {
		generateClass(program);
		return null;
	}

	byte[] generateClass(JetLangParser.ProgramContext programCtx) {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classWriter.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "Program", null, asmClassName(ProgramBase.class), null);
		createInitMethod(classWriter);
		JVMCompilationContext jvm = new JVMCompilationContext();
		createRunMethod(programCtx, classWriter, jvm);
		while (!jvm.lambdas.isEmpty()) {
			LambdaDefinition lambdaDefinition = jvm.lambdas.poll();
			generateLambda(lambdaDefinition, classWriter, jvm);
		}
		classWriter.visitEnd();
		return classWriter.toByteArray();
	}

	private void generateLambda(LambdaDefinition lambdaDefinition, ClassWriter classWriter, JVMCompilationContext jvm) {
		String args = String.join("", Collections.nCopies(lambdaDefinition.variables.size(),"Ljava/lang/Object;"));
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PRIVATE, lambdaDefinition.name, "(" + args + ")Ljava/lang/Object;", null, null);
		jvm.localVariables.clear();
		int i = 1;
		for (String variable : lambdaDefinition.variables) {
			jvm.localVariables.put(variable, i);
			i++;
		}

		jvm.methodVisitor = mv;

		mv.visitCode();
		generateExpression(lambdaDefinition.expression, jvm);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void createRunMethod(JetLangParser.ProgramContext programCtx, ClassWriter classWriter, JVMCompilationContext jvm) {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
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
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "out", "(Ljava/lang/Object;)V", false);
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
		jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", name, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
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
		else if (expr instanceof JetLangParser.MapExprContext)
			generateExpression((JetLangParser.MapExprContext)expr, jvm);
		else if (expr instanceof JetLangParser.ReduceExprContext)
			generateExpression((JetLangParser.ReduceExprContext)expr, jvm);
	}

	private void generateExpression(JetLangParser.IdentifierExprContext expr, JVMCompilationContext jvm) {
		String name = expr.identifier().IDENTIFIER().getText();
		int pos = jvm.localVariables.get(name); // todo check if declared
		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, pos);
	}

	private void generateExpression(JetLangParser.RangeExprContext expr, JVMCompilationContext jvm) {
		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); // this
		generateExpression(expr.range().expr(0), jvm);
		generateExpression(expr.range().expr(1), jvm);
		jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "newRange", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
	}

	private void generateExpression(JetLangParser.MapExprContext expr, JVMCompilationContext jvm) {
		LambdaDefinition lambdaDefinition = new LambdaDefinition();
		lambdaDefinition.expression = expr.map().expr(1);
		lambdaDefinition.variables = Collections.singletonList(expr.map().identifier().IDENTIFIER().getText());
		jvm.lambdas.add(lambdaDefinition);
		lambdaDefinition.name = "lambda_" + expr.map().identifier().start.getLine() + "_" + expr.map().identifier().start.getCharPositionInLine();

		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); // this
		generateExpression(expr.map().expr(0), jvm); // Range
		// lambdaRef
		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		jvm.methodVisitor.visitInvokeDynamicInsn("apply", "(LProgram;)Ljava/util/function/Function;",
			new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
			Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
			new Handle(Opcodes.H_INVOKESPECIAL, "Program", lambdaDefinition.name, "(Ljava/lang/Object;)Ljava/lang/Object;", false),
			Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;")
		);

		jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "map", "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", false);
	}

	private void generateExpression(JetLangParser.ReduceExprContext expr, JVMCompilationContext jvm) {
		LambdaDefinition lambdaDefinition = new LambdaDefinition();
		JetLangParser.ReduceContext reduce = expr.reduce();
		lambdaDefinition.expression = reduce.expr(2);
		lambdaDefinition.variables = reduce.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList());
		jvm.lambdas.add(lambdaDefinition);
		Token firstToken = reduce.IDENTIFIER(0).getSymbol();
		lambdaDefinition.name = "lambda_" + firstToken.getLine() + "_" + firstToken.getCharPositionInLine();

		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); // this
		generateExpression(reduce.expr(0), jvm); // Range
		generateExpression(reduce.expr(1), jvm); // initial value
		// lambdaRef
		jvm.methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		jvm.methodVisitor.visitInvokeDynamicInsn("apply", "(LProgram;)Ljava/util/function/BiFunction;",
			new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
			Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
			new Handle(Opcodes.H_INVOKESPECIAL, "Program", lambdaDefinition.name, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false),
			Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
		);
		jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "reduce", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", false);
	}

	private void generateExpression(JetLangParser.NumberExprContext expr, JVMCompilationContext jvm) {
		JetLangParser.NumberContext numberCtx = expr.number();
		if (numberCtx.INTEGER() != null) {
			Integer value = Integer.valueOf(numberCtx.INTEGER().getText());
			jvm.methodVisitor.visitIntInsn(Opcodes.SIPUSH, value);
			jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, asmClassName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else if (numberCtx.REAL_NUMBER() != null) {
			Double value = Double.valueOf(numberCtx.REAL_NUMBER().getText());
			jvm.methodVisitor.visitLdcInsn(value);
			jvm.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, asmClassName(Double.class), "valueOf", "(D)Ljava/lang/Double;", false);
		}
		else {
			throw new ThisShouldNeverHappenException("");
		}
	}

	private void createInitMethod(ClassWriter classWriter) {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, asmClassName(ProgramBase.class), "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private String asmClassName(Class clazz) {
		return clazz.getName().replace('.', '/');
	}
}
