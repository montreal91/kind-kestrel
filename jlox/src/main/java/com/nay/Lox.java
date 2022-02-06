package com.nay;

import com.nay.lox.ErrorReporter;
import com.nay.lox.ErrorToken;
import com.nay.lox.Lexer;
import com.nay.lox.Token;
import com.nay.lox.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Lox {
  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line.equals("\\q")) {
        break;
      }
      run(line);
    }
  }

  private static void run(final String source) {
    final Lexer lexer = new Lexer(source);

    Token currentToken = lexer.getNextToken();
    while (currentToken.getType() != TokenType.EOF) {
      if (currentToken.getType() == TokenType.ERROR) {
        ErrorReporter.reportLexerError((ErrorToken)currentToken);
        currentToken = lexer.getNextToken();
        continue;
      }
      System.out.println(currentToken);
      currentToken = lexer.getNextToken();
    }
  }
}
