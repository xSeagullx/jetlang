package com.xseagullx.jetlang;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Sequence {
	private static final int MAX_SIZE_FOR_TO_STRING = 40;
	public final List<Object> list;

	public Sequence(int from, int to) {
		List<Integer> list = new ArrayList<>(to + 1 - from);
		for (int i = from; i <= to; i++)
			list.add(i);
		this.list = Collections.unmodifiableList(list);
	}

	public Sequence(List<Object> list) {
		this.list = Collections.unmodifiableList(new ArrayList<>(list));
	}

	@Override public String toString() {
		if (list.size() > MAX_SIZE_FOR_TO_STRING)
			return "{" + list.get(0) + ", " + list.get(list.size() - 1) + "}";
		else
			return list.stream().map(Object::toString).collect(Collectors.joining(", "));
	}
}
