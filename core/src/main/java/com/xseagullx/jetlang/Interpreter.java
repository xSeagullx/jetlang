package com.xseagullx.jetlang;

import com.xseagullx.jetlang.runtime.stack.StackMachineCompiler;
import com.xseagullx.jetlang.utils.FileUtils;

import java.io.File;

public class Interpreter {
	public static void main(String[] args) throws Exception {
		File file = args.length > 0 ? FileUtils.validateFilePath(args[0]) : null;

		if (file == null)
			System.out.println("No file found or file cannot be read.\nExiting.");
		else {
			String text = FileUtils.readAsUTF8String(file);
			CompilationResult compilationResult = new StackMachineCompiler().parse(text);
			if (compilationResult.hasErrors()) {
				for (ParseError error : compilationResult.errors)
					System.err.println(error);
			}
			else {
				try {
					compilationResult.program.execute();
				}
				catch (Throwable ignored) {
					System.err.println("Program finished with exception");
				}
			}
		}
	}
}
