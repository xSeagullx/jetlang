package com.xseagullx.jetlang;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorState {
	private int lineNo;
	private int colNo;
	private File file;

	private final List<Runnable> subscriptions = new ArrayList<>();

	public void subscribe(Runnable r) {
		subscriptions.add(r);
	}

	private void notifySubscriptions() {
		subscriptions.forEach(Runnable::run);
	}

	public int getLineNo() {
		return lineNo;
	}

	void setLineNo(int lineNo) {
		this.lineNo = lineNo;
		notifySubscriptions();
	}

	public int getColNo() {
		return colNo;
	}

	void setColNo(int colNo) {
		this.colNo = colNo;
		notifySubscriptions();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		notifySubscriptions();
	}
}
