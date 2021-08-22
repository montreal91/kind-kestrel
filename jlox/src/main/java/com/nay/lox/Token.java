package com.nay.lox;

public class Token {
  private final TokenType type;
  private final String value;

  Token(TokenType type, String value) {
    this.type = type;
    this.value = value;
  }

  public TokenType getType() {
    return type;
  }

  String getValue() {
    return value;
  }

  public String toString() {
    return String.format("Token %s:[%s]", type.toString(), value);
  }
}
