package com.xseagullx.jetlang.services;

import java.util.function.Supplier;

public abstract class Task<T> implements Supplier<T> {
	public abstract String getId();

	@Override public String toString() {
		return getClass() + " (" + getId() + ")";
	}
}
