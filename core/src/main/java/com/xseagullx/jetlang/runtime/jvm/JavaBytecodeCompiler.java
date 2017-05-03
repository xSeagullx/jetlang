package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.CompilationResult;
import com.xseagullx.jetlang.Compiler;
import com.xseagullx.jetlang.JetLangParser;
import com.xseagullx.jetlang.runtime.jvm.ProgramBase;
import com.xseagullx.jetlang.runtime.stack.nodes.VariableExpression;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

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

		Map<String, Integer> localVariables = new HashMap<>();

		for (JetLangParser.StmtContext stmtContext : programCtx.stmt()) {
			if (stmtContext instanceof JetLangParser.DeclarationContext) {
				JetLangParser.DeclarationContext declarationCtx = (JetLangParser.DeclarationContext)stmtContext;
				String name = declarationCtx.identifier().IDENTIFIER().getText();
				Integer value = Integer.valueOf(((JetLangParser.NumberExprContext)declarationCtx.expr()).number().INTEGER().getText());

				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(7, l0);
				mv.visitIntInsn(Opcodes.SIPUSH, value);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
				mv.visitVarInsn(Opcodes.ASTORE, 1);
				localVariables.put(name, 1);
			} else if (stmtContext instanceof JetLangParser.OutExprContext) {
				JetLangParser.OutExprContext outExprContext = (JetLangParser.OutExprContext)stmtContext;
				String name = ((JetLangParser.IdentifierExprContext)outExprContext.expr()).identifier().IDENTIFIER().getText();

				mv.visitVarInsn(Opcodes.ALOAD, 0); // this
				mv.visitVarInsn(Opcodes.ALOAD, localVariables.get(name));

				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Program", "out", "(Ljava/lang/Object;)V");
			}
		}

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
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
