package com.nay.lox;


class TreeWalkEvaluator implements Expr.Visitor<Object> {

  void interpret(Expr expr) {
    try {
      Object value = evaluate(expr);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      ErrorReporter.reportRuntimeError(error);
    }
  }
  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    return switch (expr.operator.type) {
      case MINUS -> {
        checkNumberOperands(expr.operator, left, right);
        yield (double) left - (double) right;
      }
      case SLASH -> {
        checkNumberOperands(expr.operator, left, right);
        yield (double) left / (double) right;
      }
      case STAR -> {
        checkNumberOperands(expr.operator, left, right);
        yield (double) left * (double) right;
      }
      case PLUS -> processPlus(expr.operator, left, right);
      case GREATER -> {
        checkNumberOperands(expr.operator, left, right);
        yield (double) left > (double) right;
      }
      case GREATER_EQUAL -> {
        checkNumberOperands(expr.operator, left, right);
        yield (double) left >= (double) right;
      }
      case LESSER_EQUAL -> {
        checkNumberOperands(expr.operator, left, right);
        yield (double) left <= (double) right;
      }
      case BANG_EQUAL -> !isEqual(left, right);
      case EQUAL_EQUAL -> isEqual(left, right);
      default -> throw new RuntimeException(
          "Unsupported binary operator type" + expr.operator
      );
    };
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    return switch (expr.operator.type) {
      case MINUS -> processUnaryMinus(expr.operator, right);
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

  private Object processPlus(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) {
      return (double) left + (double) right;
    }
    if (left instanceof String && right instanceof String) {
      return left + (String) right;
    }
    throw new RuntimeError(
        operator,
        "Operands must be two numbers or two strings."
    );
  }

  private double processUnaryMinus(Token operator, Object operand) {
    checkNumberOperand(operator, operand);
    return -(double) operand;
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) {
      return;
    }
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) {
      return;
    }
    throw new RuntimeError(operator, "Operands must be a number");
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

  private String stringify(Object object) {
    if (object == null) {
      return "nil";
    }

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }
    return object.toString();
  }
}
