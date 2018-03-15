grammar Hello;

xq
    : '$' Letter #xqvar
    | StringSentence #xqstr
    | ap #xqap
    | '(' xq ')' #xqparenthesis
    | xq ',' xq #xqquote
    | xq '/' rp #xqslash
    | xq '//' rp #xqdoubleslash
    | '<' Letter '>' '{' xq '}' '</' Letter '>' #xqtag
    | forclause letclause ? whereclause ? returnclause  #xqflower
    | letclause  xq #xqlet
    | 'join(' xq ',' xq ',' taglist ',' taglist ')' #xqjoin
    ;

taglist
    : '[' Letter (',' Letter)* ']'
    ;

StringSentence
    : '"' [a-zA-Z0-9! _.-]+ '"'
    ;

forclause
    : 'for' '$' Letter 'in' xq (',' '$' Letter 'in' xq)* #xqforclause
    ;

letclause
    : 'let' '$' Letter ':=' xq (',' '$' Letter ':=' xq)* #xqletclause
    ;

whereclause
    : 'where' cond #xqwhereclause
    ;

returnclause
    : 'return' xq #xqreturnclause
    ;

cond
    : xq ('='|'eq') xq #condeq
    | xq ('=='|'is') xq #condis
    | 'empty' '(' xq ')' #condempty
    | 'some' '$' Letter 'in' xq (',' '$' Letter 'in' xq)* 'satisfies' cond #condsatisfy
    | '(' cond ')' #condparenthesis
    | cond 'and' cond #condand
    | cond 'or' cond #condor
    | 'not' cond #condnot
    ;

ap
	: 'doc("' Letter '")' '/' rp #apSingleSlash
	| 'doc("' Letter '")' '//' rp #apDoubleSlash
	;

rp
    : Letter #rpTagName
    | '*'   #rpAllChildren
    | '.'   #rpCurrent
    | '..'  #rpParent
    | 'text()' #rpGetTextNode
    | '@' Letter #rpGetAttribute
    | '@' '*'  #rpAllAttribute
    | '(' rp ')' #rpParenthesis
    | rp '/' rp #rpSingleSlash
    | rp '//' rp #rpDoubleSlash
    | rp '[' f ']' #rpFilter
    | rp ',' rp #rpQuote
    ;

f
	: rp #filterRp
	| NUM  #filterindex
	| rp '=' '"' Letter '"' #filterAttribute
	| rp ('='|'eq') rp #filterEq
	| rp ('=='|'is') rp #filterIs
	| '(' f ')' #filterQuote
	| f 'and' f #filterAnd
	| f 'or' f #filterOr
	| 'not' f #filterNot
	;

NUM : [0-9]+ ;

Letter : [a-zA-Z0-9!_.-]+ ;

WS  : [ \t\r\n]+ -> skip ;    // toss out whitespace