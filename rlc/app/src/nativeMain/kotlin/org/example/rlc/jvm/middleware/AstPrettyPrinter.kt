package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.Identifier
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.frontend.ast.VarDeclStmt

class AstPrettyPrinter {
  private val sb = StringBuilder()
  private var depth = 0

  fun print(roots: Ast): String {
    roots.forEach(this::visitStmt)
    return sb.toString()
  }

  private fun visitStmt(stmt: Stmt) = when (stmt) {
    is PrintStmt -> visitPrintStmt(stmt)
    is BlockStmt -> visitBlockStmt(stmt)
    is ExprStmt -> visitExprStmt(stmt)
    is VarDeclStmt -> visitVarDeclStmt(stmt)
  }

  private fun visitPrintStmt(stmt: PrintStmt) {
    addIndent()
    sb.append("PRINT:\n")
    depth++
    visitExpr(stmt.expr)
    depth--
  }

  private fun visitBlockStmt(stmt: BlockStmt) {
    addIndent()
    sb.append("BLOCK:\n")
    depth++
    stmt.statements.forEach(this::visitStmt)
    depth--
  }

  private fun visitExprStmt(stmt: ExprStmt) {
    sb.append("EXPRESSION:\n")
    depth++
    visitExpr(stmt.expr)
    depth--
  }

  private fun visitVarDeclStmt(stmt: VarDeclStmt) {
    sb.append("VAR DECL: ${stmt.identifier}\n")

    if (stmt.initializer != null) {
      depth++
      visitExpr(stmt.initializer)
      depth--
    }
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Identifier -> visitIdentifier(expr)
    is Literal -> visitLiteral(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
  }

  private fun visitBinary(expr: Binary) {
    addIndent()
    sb.append(expr.operator.type)
    sb.append("\n")
    depth++
    visitExpr(expr.left)
    visitExpr(expr.right)
    depth--
  }

  private fun visitGrouping(expr: Grouping) {
    addIndent()
    sb.append("()\n")
    depth++
    visitExpr(expr = expr.expression)
    depth--
  }

  private fun visitIdentifier(expr: Identifier) {
    addIndent()
    sb.append("Access Identifier: ${expr.identifier}\n")
  }

  private fun visitLiteral(expr: Literal) {
    addIndent()
    sb.append("${expr.type} Value: ${expr.value}\n")
  }

  private fun visitLogical(expr: Logical) {
    addIndent()
    sb.append(expr.operator.type)
    sb.append("\n")
    depth++
    visitExpr(expr.left)
    visitExpr(expr.right)
    depth--
  }

  private fun visitUnary(expr: Unary) {
    addIndent()
    sb.append("UNARY ${expr.operator.type} \n")
    depth++
    visitExpr(expr.right)
    depth--
  }

  private fun addIndent() {
    sb.append(" ".repeat(n = depth * 2))
  }
}
