package com.nay.lox;


public class Token {
  final TokenType type;
  final String value;
  final Object literal;
  final int line;

  Token(TokenType type, String value, Object literal, int line) {
    this.type = type;
    this.value = value;
    this.literal = literal;
    this.line = line;
  }

  public TokenType getType() {
    return type;
  }

  public int getLine() {
    return 0;
  }

  public String getLexeme() {
    return value;
  }

  public String toString() {
    return String.format("Token %s:[%s]", type.toString(), value);
  }
}
