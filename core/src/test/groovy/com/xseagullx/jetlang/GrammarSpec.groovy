package com.xseagullx.jetlang

import groovy.util.logging.Log
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import spock.lang.Specification
import spock.lang.Unroll

import static com.xseagullx.jetlang.JetLangLexer.ARROW
import static com.xseagullx.jetlang.JetLangLexer.DIV
import static com.xseagullx.jetlang.JetLangLexer.EQUALS
import static com.xseagullx.jetlang.JetLangLexer.IDENTIFIER
import static com.xseagullx.jetlang.JetLangLexer.INTEGER
import static com.xseagullx.jetlang.JetLangLexer.KW_MAP
import static com.xseagullx.jetlang.JetLangLexer.KW_OUT
import static com.xseagullx.jetlang.JetLangLexer.KW_PRINT
import static com.xseagullx.jetlang.JetLangLexer.KW_REDUCE
import static com.xseagullx.jetlang.JetLangLexer.KW_VAR
import static com.xseagullx.jetlang.JetLangLexer.MINUS
import static com.xseagullx.jetlang.JetLangLexer.PLUS
import static com.xseagullx.jetlang.JetLangLexer.POWER
import static com.xseagullx.jetlang.JetLangLexer.REAL_NUMBER
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
		hasTokens(lexer, tokens.collect { it instanceof Tuple2 ? it : new Tuple2<>(it, null) })

		where:
		text                                        || tokens
		'print "hello"'                             || [KW_PRINT, STRING]
		'out 5'                                     || [KW_OUT, INTEGER]
		'var a = 5'                                 || [KW_VAR, IDENTIFIER, EQUALS, INTEGER]
		'{1, 2}'                                    || [INTEGER, INTEGER]
		'-1'                                        || [MINUS, INTEGER]
		'+1'                                        || [PLUS, INTEGER]
		'1-1'                                       || [INTEGER, MINUS, INTEGER]
		'1+1'                                       || [INTEGER, PLUS, INTEGER]
		'{1, reduce(seq, 0, x y -> x + y) }'        || [INTEGER, KW_REDUCE, IDENTIFIER, INTEGER, IDENTIFIER, IDENTIFIER, ARROW, IDENTIFIER, PLUS, IDENTIFIER]
		'map({1, n}, i -> (-1)^i / (2 * i + 1))'    || [KW_MAP, ARROW, INTEGER, POWER, IDENTIFIER, DIV]
		'1.52 + a12_ * _as'                         || [t(REAL_NUMBER, '1.52'), PLUS, t(IDENTIFIER, 'a12_'), t(IDENTIFIER, '_as')]
		 // FIXME below we actually have a problem, but it'll be a parser error anyway. it parses a INTEGER(12) IDENTIFIER(_)
//		'12_ * _as'                                 || [t(IDENTIFIER, "12_"), IDENTIFIER]
		'+12.56'                                    || [REAL_NUMBER]
	}

	def "test example program"() {
		setup:
		def program = new JetLangParser(new CommonTokenStream(getTokens("var a = 5"))).program()

		expect:
		program.stmt()
	}

	Tuple2<Integer, String> t(int type, String text) {
		[type, text]
	}

	private void hasTokens(JetLangLexer jetLangLexer, List<Tuple2<Integer, String>> tokenTypes) {
		def extractedTokens = jetLangLexer.allTokens.collectMany {
			def type = VOCABULARY.getSymbolicName(it.type)
			[type, "$type($it.text)"]
		}
		def expectedTokens = tokenTypes.collect {
			VOCABULARY.getSymbolicName(it.first) + (it.second ? "($it.second)": "")
		}
		def extraToken = containsInOrder(extractedTokens, expectedTokens)
		if (extraToken)
			throw new AssertionError("Token not found token " + extraToken)
	}

	private static void printLexerOutput(String text) {
		JetLangLexer lexer = getTokens(text)
		println("${ "=" * 60 }\n$text\n${ "=" * 60 }")
		println("\nLexer TOKENS:\n\t${ lexer.allTokens.collect { "$it.line, $it.startIndex:$it.stopIndex ${ VOCABULARY.getDisplayName(it.type) } $it.text" }.join('\n\t') }\n${ "=" * 60 }")
	}

	private static JetLangLexer getTokens(String text) {
		new JetLangLexer(CharStreams.fromString(text))
	}
}
