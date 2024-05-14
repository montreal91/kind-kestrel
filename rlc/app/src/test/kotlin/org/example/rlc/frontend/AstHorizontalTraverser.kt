package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.Unary


fun testString(expr: Expr) = when (expr) {
  is Binary -> "Binary [${expr.operator.value}]"
  is Grouping -> "Grouping []"
  is Literal -> "Literal [ ${expr.value}: ${expr.type}]"
  is Logical -> "Logical [${expr.operator.value}]"
  is Unary -> "Unary [${expr.operator.value}]"
}

data class Frame(val expr: Expr, val level: Int)

fun printExpr(expr: Expr) {
  val callStack = ArrayDeque<Frame>()
  callStack.addFirst(Frame(expr = expr, level = 0))

  val valueStack = ArrayDeque<String>()

  val sb = StringBuilder()

  while (!callStack.isEmpty()) {
    val curr = callStack.first()
    sb.append(" ".repeat(n = curr.level * 4))
    sb.append(testString(curr.expr))
    sb.append("\n")

    when (curr.expr) {
      is Binary -> {
        callStack.addFirst(Frame(expr = curr.expr.right, level = curr.level + 1))
        callStack.addFirst(Frame(expr = curr.expr.left, level = curr.level + 1))
      }
      is Grouping -> callStack.addFirst(Frame(expr = curr.expr.expression, level = curr.level + 1))
      is Literal -> {}
      is Logical -> {
        callStack.addFirst(Frame(expr = curr.expr.right, level = curr.level + 1))
        callStack.addFirst(Frame(expr = curr.expr.left, level = curr.level + 1))
      }
      is Unary -> callStack.addFirst(Frame(expr = curr.expr.right, level = curr.level + 1))
    }
//    stack.removeFirst()
  }
  println(sb.toString())
}
