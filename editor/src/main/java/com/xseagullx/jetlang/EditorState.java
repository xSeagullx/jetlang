package com.xseagullx.jetlang;

import java.util.ArrayList;
import java.util.List;

class EditorState {
	private int lineNo;
	private int colNo;

	private final List<Runnable> subscriptions = new ArrayList<>();

	void subscribe(Runnable r) {
		subscriptions.add(r);
	}

	private void notifySubscriptions() {
		subscriptions.forEach(Runnable::run);
	}

	int getLineNo() {
		return lineNo;
	}

	void setLineNo(int lineNo) {
		this.lineNo = lineNo;
		notifySubscriptions();
	}

	int getColNo() {
		return colNo;
	}

	void setColNo(int colNo) {
		this.colNo = colNo;
		notifySubscriptions();
	}
}
