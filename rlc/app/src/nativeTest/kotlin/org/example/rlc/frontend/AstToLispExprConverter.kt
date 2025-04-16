package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Assignment
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.CallExpr
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.IfStmt
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.ReturnStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.frontend.ast.VarDeclStmt
import org.example.rlc.frontend.ast.Variable
import org.example.rlc.frontend.ast.WhileStmt


private fun StringBuilder.addSpaceIfNeeded(): StringBuilder {
  if (this.isNotEmpty() && !this.last().isWhitespace()) {
    this.append(' ')
  }

  return this
}

class AstToLispExprConverter(
  private val ast: List<Stmt>
) {
  private var depth = 0
  private val sb = StringBuilder()

  fun convertToLisp(): String {
    sb.append("(script\n")
    depth++

    ast.forEach(this::visitStmt)

    sb.append(")")
    return sb.toString()
  }

  private fun visitStmt(stmt: Stmt) {
    when (stmt) {
      is BlockStmt -> TODO()
      is ExprStmt -> visitExprStmt(stmt)
      is ForStmt -> TODO()
      is FunDeclStmt -> TODO()
      is IfStmt -> TODO()
      is PrintStmt -> visitPrintStmt(stmt)
      is ReturnStmt -> TODO()
      is VarDeclStmt -> TODO()
      is WhileStmt -> TODO()
    }
  }

  private fun visitExpr(expr: Expr) {
    when (expr) {
      is Assignment -> TODO()
      is Binary -> visitBinary(expr)
      is CallExpr -> TODO()
      is Grouping -> visitGrouping(expr)
      is Literal -> visitLiteral(expr)
      is Logical -> visitLogical(expr)
      is Unary -> visitUnary(expr)
      is Variable -> TODO()
    }
  }

  private fun visitExprStmt(stmt: ExprStmt) {
    addIndent()
    sb.append("(expr ")
    visitExpr(stmt.expr)
    sb.append(")\n")
  }

  private fun visitPrintStmt(stmt: PrintStmt) {
    addIndent()
    sb.append("(print ")
    visitExpr(stmt.expr)
    sb.append(")\n")
  }

  private fun visitBinary(expr: Binary) {
    sb.append("(${expr.operator.value} ")
    visitExpr(expr.left)
    visitExpr(expr.right)
    sb.append(")")
  }

  private fun visitGrouping(expr: Grouping) {
    sb.append("(group ")
    visitExpr(expr.expression)
    sb.append(")")
  }

  private fun visitLiteral(expr: Literal) {
    sb.addSpaceIfNeeded()
    sb.append(expr.value)
  }

  private fun visitLogical(expr: Logical) {
    sb.append("(${expr.operator.value} ")
    visitExpr(expr.left)
    visitExpr(expr.right)
    sb.append(")")
  }

  private fun visitUnary(expr: Unary) {
    sb.append("(${expr.operator.value}")
    visitExpr(expr.right)
    sb.append(")")
  }


  private fun addIndent() {
    sb.append(" ".repeat(n = depth * 2))
  }
}
