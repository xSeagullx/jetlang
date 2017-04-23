package com.xseagullx.jetlang;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		if (list.size() > MAX_SIZE_FOR_TO_STRING) {
			String first = join(list.stream().limit(MAX_SIZE_FOR_TO_STRING / 2));
			String last = join(list.stream().skip(MAX_SIZE_FOR_TO_STRING / 2));
			return "[" + first + " ... " + last + "]";
		}
		else
			return "[" + join(list.stream()) + "]";
	}

	private String join(Stream<Object> list) {
		return list.map(Object::toString).collect(Collectors.joining(", "));
	}
}
