package com.nay.lox;

public final class ErrorToken extends Token {
  private final int line;
  private final String message;

  ErrorToken(int line, String errorLine, String message) {
    super(TokenType.ERROR, errorLine, null, line);
    this.line = line;
    this.message = message;
  }

  public int getLine() {
    return line;
  }

  String getMessage() {
    return message;
  }
}
