# jLox

This is a java implementation of a tree-walk interpreter for a toy-language called lox.

## Current grammar
```
expression → equality
           ;

equality   → comparison ( ( "!=" | "==" ) comparison )*
           ;

comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*
           ;

term       → factor ( ( "+" | "-" ) factor) *
           ;

factor     → unary ( ( "*" | "/" ) unary) *
           ;

unary      → ( ("!" | "-") unary )
           | primary
           ;

primary    → NUMBER 
           | STRING
           | "true"
           | "false" 
           | "nil" 
           | "(" expression ")"
           ;
```