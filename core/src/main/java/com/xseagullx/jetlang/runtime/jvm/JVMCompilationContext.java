package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.ParseError;
import com.xseagullx.jetlang.runtime.CSTUtils;
import com.xseagullx.jetlang.runtime.CompilationVisitor;
import com.xseagullx.jetlang.runtime.stack.nodes.BinaryExpression;
import com.xseagullx.jetlang.utils.ThisShouldNeverHappenException;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

class JVMCompilationContext extends CompilationVisitor {
	static class LambdaDefinition {
		List<String> variables;
		JetLangParser.ExprContext expression;
		String name;
	}

	List<ParseError> errors;
	private final Map<String, Integer> localVariables = new HashMap<>();
	private final Queue<LambdaDefinition> lambdas = new ArrayDeque<>();
	private MethodVisitor methodVisitor;
	final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

	@Override public void visit(JetLangParser.ProgramContext ctx) {
		classWriter.visit(52, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "Program", null, Type.getInternalName(ProgramBase.class), null);
		createInitMethod(classWriter);
		createRunMethod(ctx);
		while (!lambdas.isEmpty()) {
			LambdaDefinition lambdaDefinition = lambdas.poll();
			generateLambda(lambdaDefinition);
		}
		classWriter.visitEnd();
	}

	@Override public void visit(JetLangParser.BinaryOpExprContext ctx) {
		pushThis();
		visit(ctx.expr(0));
		visit(ctx.expr(1));
		generateOperationCall(CSTUtils.getOperationType(ctx));
	}

	@Override public void visit(JetLangParser.NumberExprContext ctx) {
		JetLangParser.NumberContext numberCtx = ctx.number();
		Number number = CSTUtils.getNumber(numberCtx);
		if (number instanceof Integer) {
			methodVisitor.visitIntInsn(Opcodes.SIPUSH, number.intValue());
			invoke(Opcodes.INVOKESTATIC, Integer.class, "valueOf", int.class);
		}
		else if (number instanceof Double) {
			methodVisitor.visitLdcInsn(number.doubleValue());
			invoke(Opcodes.INVOKESTATIC, Double.class, "valueOf", double.class);
		}
		else {
			errors.add(new ParseError(ctx.start.getLine(), ctx.start.getCharPositionInLine() + 1, ctx.start.getStartIndex(), ctx.stop.getStopIndex(), "NumberFormatException"));
		}
	}

	@Override public void visit(JetLangParser.IdentifierExprContext ctx) {
		String name = ctx.identifier().IDENTIFIER().getText();
		Integer pos = localVariables.get(name);
		if (pos == null)
			throw new RuntimeException("Compilation exception. Undeclared variable.");
		methodVisitor.visitVarInsn(Opcodes.ALOAD, pos);
	}

	@Override public void visit(JetLangParser.RangeExprContext ctx) {
		pushThis();
		visit(ctx.range().expr(0));
		visit(ctx.range().expr(1));
		invokeBase("newRange", Object.class, Object.class);
	}

	@Override public void visit(JetLangParser.MapExprContext ctx) {
		LambdaDefinition lambdaDefinition = createLambdaDefinition(ctx.map().expr(1), Collections.singletonList(ctx.map().identifier().IDENTIFIER()));

		pushThis();
		visit(ctx.map().expr(0)); // Range
		// lambdaRef
		pushThis();
		methodVisitor.visitInvokeDynamicInsn("apply", "(LProgram;)Ljava/util/function/Function;",
			new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
			Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
			new Handle(Opcodes.H_INVOKESPECIAL, "Program", lambdaDefinition.name, "(Ljava/lang/Object;)Ljava/lang/Object;", false),
			Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;")
		);

		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "map", "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", false);
	}

	@Override public void visit(JetLangParser.ReduceExprContext ctx) {
		JetLangParser.ReduceContext reduce = ctx.reduce();
		LambdaDefinition lambdaDefinition = createLambdaDefinition(reduce.expr(2), reduce.IDENTIFIER());

		pushThis();
		visit(reduce.expr(0)); // Range
		visit(reduce.expr(1)); // initial value
		// lambdaRef
		pushThis();
		methodVisitor.visitInvokeDynamicInsn("apply", "(LProgram;)Ljava/util/function/BiFunction;",
			new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
			Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
			new Handle(Opcodes.H_INVOKESPECIAL, "Program", lambdaDefinition.name, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false),
			Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
		);
		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "reduce", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", false);
	}

	@Override public void visit(JetLangParser.DeclarationContext ctx) {
		String name = ctx.identifier().IDENTIFIER().getText();
		visit(ctx.expr());
		int pos = localVariables.size() + 1;
		methodVisitor.visitVarInsn(Opcodes.ASTORE, pos);
		localVariables.put(name, pos);
	}

	@Override public void visit(JetLangParser.OutExprContext ctx) {
		pushThis();
		visit(ctx.expr());
		invokeBase("out", Object.class);
	}

	@Override public void visit(JetLangParser.PrintExprContext ctx) {
		String text = ctx.STRING().getText();
		text = "".equals(text) ? "" : text.substring(1, text.length() - 1);
		pushThis();
		methodVisitor.visitLdcInsn(text);
		invokeBase("out", Object.class);
	}

	// Utility methods

	private void generateLambda(LambdaDefinition lambdaDefinition) {
		String args = String.join("", Collections.nCopies(lambdaDefinition.variables.size(),"Ljava/lang/Object;"));
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PRIVATE, lambdaDefinition.name, "(" + args + ")Ljava/lang/Object;", null, null);
		localVariables.clear();
		int i = 1;
		for (String variable : lambdaDefinition.variables) {
			localVariables.put(variable, i);
			i++;
		}

		methodVisitor = mv;

		mv.visitCode();
		visit(lambdaDefinition.expression);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void createRunMethod(JetLangParser.ProgramContext programCtx) {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
		methodVisitor = mv;

		for (JetLangParser.StmtContext stmtContext : programCtx.stmt())
			visit(stmtContext);

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void createInitMethod(ClassWriter classWriter) {
		MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ProgramBase.class), "<init>", "()V", false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void pushThis() {
		methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
	}

	private void generateOperationCall(BinaryExpression.OperationType operationType) {
		String name = operationType.name().toLowerCase();
		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", name, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
	}

	private void invoke(int opcode, Class<?> owner, String method, Class<?> ... params) {
		try {
			String descriptor = Type.getMethodDescriptor(owner.getDeclaredMethod(method, params));
			methodVisitor.visitMethodInsn(opcode, Type.getInternalName(owner), method, descriptor, false);
		}
		catch (NoSuchMethodException e) {
			throw new ThisShouldNeverHappenException("Wrong method call from jetlang to bytecode compiler.");
		}
	}

	private void invokeBase(String method, Class<?> ... params) {
		Class<ProgramBase> owner = ProgramBase.class;
		try {
			String descriptor = Type.getMethodDescriptor(owner.getDeclaredMethod(method, params));
			methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", method, descriptor, false);
		}
		catch (NoSuchMethodException e) {
			throw new ThisShouldNeverHappenException("Wrong method call from jetlang to bytecode compiler.");
		}
	}

	private LambdaDefinition createLambdaDefinition(JetLangParser.ExprContext ctx, List<TerminalNode> arguments) {
		LambdaDefinition definition = new LambdaDefinition();
		definition.expression = ctx;
		definition.variables = arguments.stream().map(ParseTree::getText).collect(Collectors.toList());
		Token firstToken = arguments.get(0).getSymbol();
		definition.name = "lambda_" + firstToken.getLine() + "_" + (firstToken.getCharPositionInLine() + 1);
		lambdas.add(definition);
		return definition;
	}
}
