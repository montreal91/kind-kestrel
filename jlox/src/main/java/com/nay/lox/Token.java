package com.nay.lox;

public class Token {
  final TokenType type;
  final String value;

  Token(TokenType type, String value) {
    this.type = type;
    this.value = value;
  }

  public TokenType getType() {
    return type;
  }

  public int getLine() {
    return 0;
  }

  public String getLexeme() {
    return "";
  }

  public String toString() {
    return String.format("Token %s:[%s]", type.toString(), value);
  }
}
