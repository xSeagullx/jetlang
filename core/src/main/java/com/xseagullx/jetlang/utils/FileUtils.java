package com.xseagullx.jetlang.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {
	public static File validateFilePath(String path) {
		File file = new File(path);
		if (!file.canRead()) {
				return null;
			}
		return file;
	}

	public static String readAsUTF8String(File file) throws IOException {
		byte[] bytes = Files.readAllBytes(file.toPath());
		return new String(bytes, "UTF-8");
	}
}
