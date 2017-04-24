lexer grammar JetLangLexer;

@header {
	package com.xseagullx.jetlang;
}


STRING: '"' .*? '"' ;
WS:  [ \t]+ -> skip ;

// Numbers
fragment SIGN: ('-'|'+') ;
fragment DIGIT: [0-9] ;
fragment DOT: '.' ;
INTEGER: SIGN? DIGIT+ ;
REAL_NUMBER: SIGN? DIGIT+ (DOT DIGIT+)? ;

// Operators
MUL: '*' ;
DIV: '/' ;
PLUS: '+' ;
MINUS: '-' ;
POWER: '^' ;

// Strings

// Lambda fragments
ARROW: '->' ;
OPEN_CURLY: '{' ;
CLOSE_CURLY: '}' ;
OPEN_PAR: '(' ;
CLOSE_PAR: ')' ;
COMA: ',' ;
EQUALS: '=' ;

// Keywords
KW_REDUCE: 'reduce' ;
KW_MAP: 'map' ;
KW_PRINT: 'print' ;
KW_VAR: 'var' ;
KW_OUT: 'out' ;

NL: '\r'? '\n' ;

IDENTIFIER: [A-Za-z_][A-Za-z_0-9]* ;
