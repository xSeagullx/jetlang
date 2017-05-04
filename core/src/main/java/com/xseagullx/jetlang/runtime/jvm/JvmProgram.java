package com.xseagullx.jetlang.runtime.jvm;

import com.xseagullx.jetlang.ExecutionContext;
import com.xseagullx.jetlang.Program;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureClassLoader;

class JetLangClassLoader extends SecureClassLoader {
	Class<ProgramBase> programClass;

	JetLangClassLoader(byte[] bytes) {
		//noinspection unchecked
		programClass = (Class<ProgramBase>)defineClass(null, bytes, 0, bytes.length);
	}
}

public class JvmProgram implements Program {
	public final byte[] bytes;

	JvmProgram(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override public void execute() {
		execute(null);
	}

	@Override public void execute(ExecutionContext existingContext) {
		try {
			ProgramBase programBase = loadClass().newInstance();
			programBase.context = existingContext;
			try {
				programBase.run();
			}
			catch (Throwable t) {
				printExceptionToContext(existingContext, t);
				throw new RuntimeException("Error executing program", t);
			}
		}
		catch (InstantiationException | IllegalAccessException e) {
			printExceptionToContext(existingContext, e);
			throw new RuntimeException("Error instantiating program", e);
		}
	}

	private void printExceptionToContext(ExecutionContext context, Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		context.error(writer.toString());
	}

	Class<ProgramBase> loadClass() {
		// Class will keep a reference to classloader
		return new JetLangClassLoader(bytes).programClass;
	}
}
