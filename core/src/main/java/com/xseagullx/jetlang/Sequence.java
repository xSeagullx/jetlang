package com.xseagullx.jetlang;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sequence {
	private static final int MAX_SIZE_FOR_TO_STRING = 20;
	public final List<Object> list;

	public Sequence(int from, int to) {
		Integer[] array = new Integer[to + 1 - from];
		for (int i = from; i <= to; i++) {
			array[i - from] = i;
		}
		this.list = Collections.unmodifiableList(Arrays.asList(array));
	}

	public Sequence(List<Object> list) {
		this.list = Collections.unmodifiableList(new ArrayList<>(list));
	}

	@Override public String toString() {
		if (list.size() > MAX_SIZE_FOR_TO_STRING) {
			String first = join(list.stream().limit(MAX_SIZE_FOR_TO_STRING / 2));
			String last = join(list.stream().skip(list.size() - MAX_SIZE_FOR_TO_STRING / 2));
			return "[" + first + " ... " + last + "]";
		}
		else
			return "[" + join(list.stream()) + "]";
	}

	private String join(Stream<Object> list) {
		return list.map(Object::toString).collect(Collectors.joining(", "));
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Sequence sequence = (Sequence)o;
		return Objects.equals(list, sequence.list);
	}

	@Override public int hashCode() {
		return Objects.hash(list);
	}
}
