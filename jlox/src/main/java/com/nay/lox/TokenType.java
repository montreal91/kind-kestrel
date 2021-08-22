package com.nay.lox;

public enum TokenType {
  // Single-character token
  COMMA, DOT, MINUS, PLUS, SEMI, SLASH, STAR,
  LPAR, RPAR, LCURL, RCURL,

  // One or two char tokens
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESSER, LESSER_EQUAL,

  // Literals
  ID, NUMBER, STRING,

  // keywords
  AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT,
  RETURN, SUPER, THIS, TRUE, VAR, WHILE,

  EOF, ERROR
}
