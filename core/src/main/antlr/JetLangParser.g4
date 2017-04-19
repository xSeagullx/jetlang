parser grammar JetLangParser;

@header {
	package com.xseagullx.jetlang;
}

options { tokenVocab = JetLangLexer; }

programm: (NL | stmt)* EOF;

range: OPEN_CURLY expr COMA expr CLOSE_CURLY ;
identifier: IDENTIFIER ;
number: INTEGER | REAL_NUMBER ;
mapExpr: KW_MAP OPEN_PAR expr COMA identifier ARROW expr CLOSE_PAR ;
reduceExpr: KW_REDUCE OPEN_PAR expr COMA expr COMA IDENTIFIER IDENTIFIER ARROW expr CLOSE_PAR;

expr
	: expr POWER expr
	| expr (MUL | DIV) expr
	| expr (PLUS | MINUS) expr
	| OPEN_PAR expr CLOSE_PAR
	| identifier
	| range
	| number
	| mapExpr
	| reduceExpr
	;

declaration: KW_VAR identifier EQUALS expr ;

stmt
	: declaration
	| KW_OUT expr
	| KW_PRINT STRING
	| expr
	;
