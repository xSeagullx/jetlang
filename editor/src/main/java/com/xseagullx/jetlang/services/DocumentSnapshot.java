package com.xseagullx.jetlang.services;

public class DocumentSnapshot {
	private static int i = 0;
	public final String text;
	private int id = i++;

	public DocumentSnapshot(String text) {
		this.text = text;
	}

	int getId() {
		return id;
	}
}
