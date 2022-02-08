package com.nay.lox;

public class Interpreter {
  public static Object interpret(String text) {
    Lexer lexer = new Lexer(text);
    Parser parser = new Parser(lexer.getAllTokens());
    Expr e = parser.parse();

    TreeWalkEvaluator evaluator = new TreeWalkEvaluator();
    return e.accept(evaluator);
  }
}
