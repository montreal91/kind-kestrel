package com.nay.lox;

import java.util.List;

public class InterpreterFacade {
  public static void interpret(String text) {
    Lexer lexer = new Lexer(text);
    Parser parser = new Parser(lexer.getAllTokens());
    List<Stmt> statements = parser.parse();

    Interpreter evaluator = new Interpreter();
    evaluator.interpret(statements);
  }
}
