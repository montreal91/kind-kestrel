package com.nay.lox;

public class ErrorReporter {
  public static void reportErrorToken(ErrorToken error) {
    System.err.println(
        "[line " + error.getLine() + "] Error. " + error.getMessage() + "\n" + error.getValue()
    );
  }
}
