package com.nay.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new LinkedList<>();

    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Stmt declaration() {
    try {

      if (match(TokenType.VAR)) {
        return varDeclaration();
      }

      if (match(TokenType.FUN)) {
        return function("function");
      }
      return statement();

    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt.Function function(
      @SuppressWarnings("SameParameterValue") String kind
  ) {
    Token name = consume(
        TokenType.IDENTIFIER,
        "Expect " + kind + " name."
    );
    consume(TokenType.LPAR, "Expect '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(TokenType.RPAR)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters");
        }

        parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RPAR, "Expect ')' after parameters.");
    consume(TokenType.LCURL, "Expect '{' before " + kind + " body.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);

  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

    Expr initializer = null;

    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(TokenType.SEMI, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.IF)) {
      return ifStatement();
    }

    if (match(TokenType.PRINT)) {
      return printStatement();
    }

    if (match(TokenType.WHILE)) {
      return whileStatement();
    }

    if (match(TokenType.FOR)) {
      return forStatement();
    }

    if (match(TokenType.LCURL)) {
      return new Stmt.Block(block());
    }

    if (match(TokenType.RETURN)) {
      return returnStatement();
    }

    return expressionStatement();
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;

    if (!check(TokenType.SEMI)) {
      value = expression();
    }

    consume(TokenType.SEMI, "Expect ';' after return value.");
    return new Stmt.Return(keyword, value);
  }

  private Stmt ifStatement() {
    consume(TokenType.LPAR, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(TokenType.RPAR, "Expect ')' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;

    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new LinkedList<>();

    while (!check(TokenType.RCURL) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RCURL, "Expect '}' after block.");
    return statements;
  }

  private Stmt printStatement() {
    Expr expr = expression();
    consume(TokenType.SEMI, "Expect ';' after value.");
    return new Stmt.Print(expr);
  }

  private Stmt whileStatement() {
    consume(TokenType.LPAR, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(TokenType.RPAR, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt forStatement() {
    consume(TokenType.LPAR, "Expect '(' after 'for'.");

    Stmt initializer;

    if (match(TokenType.SEMI)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;

    if (!check(TokenType.SEMI)) {
      condition = expression();
    }

    consume(TokenType.SEMI, "Expect ';' after loop condition.");

    Expr increment = null;

    if (!check(TokenType.RPAR)) {
      increment = expression();
    }

    consume(TokenType.RPAR, "Expect ')' after for clauses.");
    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(
          body,
          new Stmt.Expression(increment)
      ));
    }

    if (condition == null) {
      condition = new Expr.Literal(true);
    }

    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt expressionStatement() {
    Expr value = expression();
    consume(TokenType.SEMI, "Expect ';' after value.");
    return new Stmt.Expression(value);
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = or();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

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

    return call();
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      if (match(TokenType.LPAR)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(TokenType.RPAR)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }

        arguments.add(expression());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RPAR, "Expect ')' after arguments");
    return new Expr.Call(callee, paren, arguments);
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
      if (previous().type == TokenType.NUMBER) {
        return new Expr.Literal(Double.parseDouble(previous().value));
      }
      return new Expr.Literal(previous().value);
    }

    if (match(TokenType.LPAR)) {
      Expr expression = expression();
      consume(TokenType.RPAR, "Expect ')' after expression.");
      return new Expr.Grouping(expression);
    }

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    throw error(peek(), "Expect expression.");
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

  private Token consume(TokenType type, String message) {
    if (check(type)) {
      return advance();
    }

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    ErrorReporter.reportError(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMI) {
        return;
      }

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
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
