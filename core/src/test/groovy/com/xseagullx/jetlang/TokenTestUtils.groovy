package com.xseagullx.jetlang

trait TokenTestUtils {
	def <T> boolean containsInOrder(List<T> toCheck, List<T> expected) {
		def next = expected.iterator()
		def nextExpected = proceedToNextExpected(next)

		for (elem in toCheck) {
			if (nextExpected == null)
				return true

			if (nextExpected == elem)
				nextExpected = proceedToNextExpected(next)
		}

		return nextExpected == null
	}

	private <T> T proceedToNextExpected(Iterator<T> next) {
		next.hasNext() ? next.next() : null
	}
}
