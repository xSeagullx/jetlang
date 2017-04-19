package com.xseagullx.jetlang

import groovy.util.logging.Log
import org.antlr.v4.runtime.CharStreams
import spock.lang.Specification
import spock.lang.Unroll

import static com.xseagullx.jetlang.JetLangLexer.ARROW
import static com.xseagullx.jetlang.JetLangLexer.EQUALS
import static com.xseagullx.jetlang.JetLangLexer.IDENTIFIER
import static com.xseagullx.jetlang.JetLangLexer.INTEGER
import static com.xseagullx.jetlang.JetLangLexer.KW_OUT
import static com.xseagullx.jetlang.JetLangLexer.KW_PRINT
import static com.xseagullx.jetlang.JetLangLexer.KW_REDUCE
import static com.xseagullx.jetlang.JetLangLexer.KW_VAR
import static com.xseagullx.jetlang.JetLangLexer.PLUS
import static com.xseagullx.jetlang.JetLangLexer.STRING
import static com.xseagullx.jetlang.JetLangLexer.VOCABULARY

@Log
class GrammarSpec extends Specification implements TokenTestUtils {
	@Unroll("Checking lexer results for: #text")
	def "test lexer token types"() {
		when:
		def lexer = getTokens(text)
		printLexerOutput(text)

		then:
		hasTokens(lexer, tokens)

		where:
		text                                        || tokens
		'print "hello"'                             || [KW_PRINT, STRING]
		'out 5'                                     || [KW_OUT, INTEGER]
		'var a = 5'                                 || [KW_VAR, IDENTIFIER, EQUALS, INTEGER]
		'{1, 2}'                                    || [INTEGER, INTEGER]
		'{1, reduce(seq, 0, x y -> x + y) }'        || [INTEGER, KW_REDUCE, IDENTIFIER, INTEGER, IDENTIFIER, IDENTIFIER, ARROW, IDENTIFIER, PLUS, IDENTIFIER]
	}

	private boolean hasTokens(JetLangLexer jetLangLexer, List<Integer> tokenTypes) {
		containsInOrder(jetLangLexer.allTokens.collect { it.type }, tokenTypes)
	}

	private static void printLexerOutput(String text) {
		JetLangLexer lexer = getTokens(text)
		println("${ "=" * 60 }\n$text\n${ "=" * 60 }")
		println("\nLexer TOKENS:\n\t${ lexer.allTokens.collect { "$it.line, $it.startIndex:$it.stopIndex ${ VOCABULARY.getDisplayName(it.type) } $it.text" }.join('\n\t') }${ "=" * 60 }")
	}

	private static JetLangLexer getTokens(String text) {
		new JetLangLexer(CharStreams.fromString(text))
	}
}
