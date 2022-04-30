# jLox

This is a java implementation of a tree-walk interpreter for a toy-language called 
**[Lox](https://craftinginterpreters.com/the-lox-language.html)**.

## Current grammar
```
program        → declaration* EOF
               ;

declaration    → varDecl;
               | funDecl
               | classDecl
               | statement
               ;

varDecl        → "var" IDENTIFIER ( = expression )? ";"
               ;

funDecl        → "fun" function
               ;
               
classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}"
               ;

function       → IDENTIFIER "(" parameters? ")" block
               ;

parameters     → IDENTIFIER ( "," IDENTIFIER )*
               ;

statement      → exprStmt
               | printStmt
               | ifStmt
               | whileStmt
               | forStmt
               | block
               | returnStmt
               ;

whileStmt      → "while" "(" expression ")" statement
               ;

forstmt        → "for" "(" ( varDecl | exprStmt | ";" ) 
                 expression? ";" 
                 expression? ")" 
                 statement
               ;

exprStmt       → expression ";" 
               ;

ifStmt         → "if" "(" expression ")" statement ("else" statement )? 
               ;

printStmt      → "print" expression ";" 
               ;

block          → "{" declaration* "}"
               ;

returnStmt     → "return expression? ";"
               ;

expression     → assignment
               ;

assignment     → ( call "." )? IDENTIFIER "=" assignment
               | logic_or
               ;
               
logic_or       → logic_and ( "or" logic_and )*
               ;

logic_and      → equality ( "and" equality )*
               ;

equality       → comparison ( ( "!=" | "==" ) comparison )*
               ;

comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )*
               ;

term           → factor ( ( "+" | "-" ) factor) *
               ;

factor         → unary ( ( "*" | "/" ) unary) *
               ;

unary          → ( ("!" | "-") unary )
               | call
               ;

call           → primary ( "(" arguments? ")" | "." IDENTIFIER )*
               ;

arguments      → expression ( "," expression )*
               ;

primary        → NUMBER
               | STRING
               | "true"
               | "false"
               | "nil"
               | "(" expression ")"
               | IDENTIFIER
               ;
```
