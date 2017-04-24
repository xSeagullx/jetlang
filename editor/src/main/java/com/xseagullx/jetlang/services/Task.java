package com.xseagullx.jetlang.services;

import java.util.concurrent.Callable;

public abstract class Task<T> implements Callable<T> {
	public abstract String getId();

	@Override public String toString() {
		return getClass() + " (" + getId() + ")";
	}
}
