package com.nay.lox;


public class TreeWalkEvaluator implements Expr.Visitor<Object> {
  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    return switch (expr.operator.type) {
      case MINUS -> (double)left - (double) right;
      case SLASH -> (double) left / (double) right;
      case STAR -> (double) left * (double) right;
      case PLUS -> processPlus(left, right);
      case GREATER -> (double) left > (double) right;
      case GREATER_EQUAL -> (double) left >= (double) right;
      case LESSER -> (double) left < (double) right;
      case LESSER_EQUAL -> (double) left <= (double) right;
      case BANG_EQUAL -> !isEqual(left, right);
      case EQUAL_EQUAL -> isEqual(left, right);
      default ->
          // unreachable
          null;
    };
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    return switch (expr.operator.type) {
      case MINUS -> -(double) right;
      case BANG -> !isTruthy(right);
      default ->
          // Unreachable
          right;
    };

  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private boolean isTruthy(Object object) {
    if (object == null) {
      return false;
    }
    if (object instanceof Boolean) {
      return (boolean) object;
    }
    return true;
  }

  private boolean isEqual(Object left, Object right) {
    if (left == null && right == null) {
      return true;
    }
    if (left == null) {
      return false;
    }
    return left.equals(right);
  }

  private Object processPlus(Object left, Object right) {
    if (left instanceof Double && right instanceof Double) {
      return (double) left + (double) right;
    }
    if (left instanceof String && right instanceof String) {
      return left + (String) right;
    }
    throw new RuntimeException("Bad values for summation.");
  }
}
