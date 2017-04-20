parser grammar JetLangParser;

@header {
	package com.xseagullx.jetlang;
}

options { tokenVocab = JetLangLexer; }

program: (NL | stmt)* EOF;

range: OPEN_CURLY expr COMA expr CLOSE_CURLY ;
identifier: IDENTIFIER ;
number: INTEGER | REAL_NUMBER ;
map: KW_MAP OPEN_PAR expr COMA identifier ARROW expr CLOSE_PAR ;
reduce : KW_REDUCE OPEN_PAR expr COMA expr COMA IDENTIFIER IDENTIFIER ARROW expr CLOSE_PAR;

expr
	: expr POWER expr #binaryOpExpr
	| expr (MUL | DIV) expr #binaryOpExpr
	| expr (PLUS | MINUS) expr #binaryOpExpr
	| OPEN_PAR expr CLOSE_PAR #parenthesisExpr
	| identifier #identifierExpr
	| range #rangeExpr
	| number #numberExpr
	| map #mapExpr
	| reduce #reduceExpr
	;

stmt
	: KW_VAR identifier EQUALS expr #declaration
	| KW_OUT expr #outExpr
	| KW_PRINT STRING #printExpr
	;
