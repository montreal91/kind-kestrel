package com.nay.lox;

import java.util.List;

public class InterpreterFacade {
  public static void interpret(String text) {
    Lexer lexer = new Lexer(text);
    Parser parser = new Parser(lexer.getAllTokens());
    List<Stmt> statements = parser.parse();

    Interpreter interpreter = new Interpreter();
    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);
    interpreter.interpret(statements);
  }
}
