package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary


data class Frame(
  val expr: Expr,
  var visitedLeft: Boolean = false,
  var visitedRight: Boolean = false,
)

private fun StringBuilder.addSpaceIfNeeded(): StringBuilder {
  if (this.isNotEmpty() && !this.last().isWhitespace()) {
    this.append(' ')
  }
  return this
}

private fun StringBuilder.trimTrailingWhitespace(): StringBuilder {
  var i = this.length - 1
  while (i >= 0 && this[i].isWhitespace()) {
    i--
  }
  if (i < this.length - 1) {
    this.setLength(i + 1)
  }
  return this
}

class AstToLispExprConverter(
  private val ast: List<Stmt>
) {
  fun convertToLisp(): String {
    val sb = StringBuilder()
    for (expr in ast) {
      printStmt(expr, sb)
    }

    sb.trimTrailingWhitespace()
    return sb.toString()
  }
}

private fun printStmt(stmt: Stmt, sb: StringBuilder) {
  when (stmt) {
    is ExprStmt -> {
      sb.append('(').append("expr ")
      printExpr(stmt.expr, sb)
      sb.append(')')
      sb.append('\n')
    }
    is PrintStmt -> {
      sb.append('(')
      sb.append("print")
      printExpr(stmt.expr, sb)
      sb.append(')')
      sb.append('\n')
    }
  }
}

private fun printExpr(expr: Expr, sb: StringBuilder) {
  val callStack = ArrayDeque<Frame>()
  callStack.addFirst(Frame(expr = expr))

  while (!callStack.isEmpty()) {
    val curr = callStack.first()

    when (curr.expr) {
      is Binary -> {
        if (!curr.visitedLeft) {
          callStack.addFirst(Frame(expr = curr.expr.left))
          curr.visitedLeft = true
          sb.addSpaceIfNeeded()
          sb.append("(${curr.expr.operator.value} ")
          continue
        }
        if (!curr.visitedRight) {
          callStack.addFirst(Frame(expr = curr.expr.right))
          curr.visitedRight = true
          continue
        }
        sb.append(")")
        callStack.removeFirst()
      }

      is Grouping -> {
        if (!curr.visitedLeft) {
          callStack.addFirst(Frame(expr = curr.expr.expression))
          curr.visitedLeft = true
          curr.visitedRight = true
          sb.addSpaceIfNeeded()
          sb.append("(group ")
          continue
        }
        sb.append(")")
        callStack.removeFirst()
      }
      is Literal -> {
        sb.addSpaceIfNeeded()
        sb.append(curr.expr.value)
        callStack.removeFirst()
      }
      is Logical -> {
        if (!curr.visitedLeft) {
          callStack.addFirst(Frame(expr = curr.expr.left))
          curr.visitedLeft = true
          sb.addSpaceIfNeeded()
          sb.append("(${curr.expr.operator.value} ")
          continue
        }
        if (!curr.visitedRight) {
          callStack.addFirst(Frame(expr = curr.expr.right))
          curr.visitedRight = true
          continue
        }

        sb.append(")")
        callStack.removeFirst()
      }

      is Unary -> {
        if (!curr.visitedLeft) {
          callStack.addFirst(Frame(expr = curr.expr.right))
          curr.visitedLeft = true
          curr.visitedRight = true
          sb.addSpaceIfNeeded()
          sb.append("(${curr.expr.operator.value} ")
          continue
        }

        sb.append(")")
        callStack.removeFirst()
      }
    }
  }
}
