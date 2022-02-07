package com.nay.lox;

public class ErrorReporter {
  public static void reportLexerError(ErrorToken error) {
    System.err.println(
        "[line " + error.getLine() + "] Error. " + error.getMessage() + "\n" + error.value
    );
  }

  public static void reportParserError(Token token, String message) {
    if (token.getType() == TokenType.EOF) {
      report(token.getLine(), " at end", message);
    } else {
      report(token.getLine(), " at '" + token.getLexeme() + "'", message);
    }
  }

  private static void report(int line, String at, String message) {
  }
}
