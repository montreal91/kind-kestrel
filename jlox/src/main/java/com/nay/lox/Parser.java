package com.nay.lox;

import java.util.List;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }


  /**
   * expression → equality
   */
  private Expr expression() {
    return equality();
  }

  /**
   * equality → comparison ( ( "!=" | "==" ) comparison )* ;
   */
  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }


  /**
   * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
   */
  private Expr comparison() {
    Expr expr = term();
    TokenType[] comparisons = new TokenType[] {
        TokenType.GREATER,
        TokenType.GREATER_EQUAL,
        TokenType.LESSER,
        TokenType.LESSER_EQUAL
    };
    while (match(comparisons)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.PLUS, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.STAR, TokenType.SLASH)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return primary();
  }

  private Expr primary() {
    if (match(TokenType.FALSE)) {
      return new Expr.Literal(false);
    }
    if (match(TokenType.TRUE)) {
      return new Expr.Literal(true);
    }
    if (match(TokenType.NIL)) {
      return new Expr.Literal(null);
    }
    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().value);
    }

    if (match(TokenType.LPAR)) {
      Expr expression = expression();
      consume(TokenType.RPAR, "Expect ')' after expression.");
      return new Expr.Grouping(expression);
    }
    throw new RuntimeException("This should never happen.");
  }

  private void consume(TokenType rpar, String s) {
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) {
      current++;
    }
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }
}
