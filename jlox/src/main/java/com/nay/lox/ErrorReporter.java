package com.nay.lox;

public class ErrorReporter {
  private static boolean hadRuntimeError = false;

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

  public static void reportRuntimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  public static boolean hadRuntimeErrors() {
    return hadRuntimeError;
  }
}
