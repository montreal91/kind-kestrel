# jLox

This is a java implementation of a tree-walk interpreter for a toy-language called **Lox**.

## Current grammar
```
program        → declaration* EOF
               ;

declaration    → varDecl;
               | statement
               ;

varDecl        → "var" IDENTIFIER ( = expression )? ";"
               ;

statement      → exprStmt
               | ifStmt
               | printStmt
               | block
               ;

exprStmt       → expression ";" 
               ;

ifStmt         → "if" "(" expression ")" statement ("else" statement )? 
               ;

printStmt      → "print" expression ";" 
               ;

block          → "{" declaration* "}"
               ;

expression     → assignment
               ;

assignment     → IDENTIFIER "=" assignment
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
               | primary
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
