package com.nay.lox;

public class TreeWalkEvaluator implements Expr.Visitor<Double> {
  @Override
  public Double visitBinaryExpr(Expr.Binary expr) {
    Double left = expr.left.accept(this);
    Double right = expr.right.accept(this);
    if (expr.operator.type == TokenType.PLUS) {
      return left + right;
    }
    if (expr.operator.type == TokenType.MINUS) {
      return  left - right;
    }
    if (expr.operator.type == TokenType.STAR) {
      return left * right;
    }
    if (expr.operator.type == TokenType.SLASH) {
      return left / right;
    }
    throw new RuntimeException("This should never happen... " + expr);
  }

  @Override
  public Double visitGroupingExpr(Expr.Grouping expr) {
    return expr.expression.accept(this);
  }

  @Override
  public Double visitLiteralExpr(Expr.Literal expr) {
    return (Double) expr.value;
  }

  @Override
  public Double visitUnaryExpr(Expr.Unary expr) {
    Double value = expr.right.accept(this);
    if (expr.operator.type == TokenType.MINUS) {
      value *= -1;
    }
    return value;
  }
}
