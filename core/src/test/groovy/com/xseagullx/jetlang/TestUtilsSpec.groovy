package com.xseagullx.jetlang

import spock.lang.Specification
import spock.lang.Unroll

class TestUtilsSpec extends Specification implements TokenTestUtils {
	@SuppressWarnings("GroovyPointlessBoolean")
	@Unroll("[1, 2, 3, 4, 5] contains in order #expectedElements is #result")
	def "test containsInOrder"() {
		setup:
		def listToCheck = (1..5).toList()

		when:
		def success = containsInOrder(listToCheck, expectedElements) == null

		then:
		success == result

		where:
		result   || expectedElements
		true     || [1]
		true     || [4]
		true     || [1, 4]
		false    || [3, 2]
		true     || []
		false    || [1, 1, 2, 3]
		true     || [1, 2, 3, 4, 5]
		false    || [1, 2, 3, 4, 5, 6]
	}
}
