package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.ast.Assignment
import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.IfStmt
import org.example.rlc.frontend.ast.Variable
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.ReturnStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.Unary
import org.example.rlc.frontend.ast.VarDeclStmt
import org.example.rlc.frontend.ast.WhileStmt

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
    is IfStmt -> visitIfStmt(stmt)
    is WhileStmt -> visitWhileStmt(stmt)
    is ForStmt -> visitForStmt(stmt)
    is FunDeclStmt -> visitFunDeclStmt(stmt)
    is ReturnStmt -> visitReturnStmt(stmt)
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
    addIndent()
    sb.append("EXPRESSION:\n")
    depth++
    visitExpr(stmt.expr)
    depth--
  }

  private fun visitReturnStmt(stmt: ReturnStmt) {
    addIndent()
    sb.append("RETURN")
    stmt.expr?.let {
      sb.append(":\n")
      depth++
      visitExpr(stmt.expr)
      depth--
    } ?: sb.append("\n")
  }

  private fun visitVarDeclStmt(stmt: VarDeclStmt) {
    addIndent()
    sb.append("VAR DECL: ${stmt.variable}\n")

    if (stmt.initializer != null) {
      depth++
      visitExpr(stmt.initializer)
      depth--
    }
  }

  private fun visitFunDeclStmt(stmt: FunDeclStmt) {
    addIndent()
    sb.append("FUNCTION: ${stmt.identifier.value} (")
    sb.append(stmt.parameters.joinToString(separator = ", ") { it.value })
    sb.append(")\n")
    depth++
    visitBlockStmt(stmt.body)
    depth--
  }

  private fun visitIfStmt(stmt: IfStmt) {
    addIndent()
    sb.append("IF:\n")
    depth++
    visitExpr(stmt.expr)
    visitStmt(stmt.ifBranch)
    depth--

    stmt.elseBranch?.let {
      addIndent()
      sb.append("ELSE:\n")
      depth++
      visitStmt(stmt.elseBranch)
      depth--
    }
  }

  private fun visitWhileStmt(stmt: WhileStmt) {
    addIndent()
    sb.append("WHILE:\n")
    depth++
    visitExpr(stmt.expr)
    visitStmt(stmt.body)
    depth--
  }

  private fun visitForStmt(stmt: ForStmt) {
    addIndent()
    sb.append("FOR:\n")
    depth++
    stmt.initStmt?.let{
      addIndent()
      sb.append("INIT STMT:\n")
      depth++
      visitStmt(stmt.initStmt)
      depth--
    }
    stmt.conditionExpr?.let {
      addIndent()
      sb.append("COND EXPR:\n")
      depth++
      visitExpr(stmt.conditionExpr)
      depth--
    }
    stmt.updateExpr?.let {
      addIndent()
      sb.append("UPD EXPR:\n")
      depth++
      visitExpr(stmt.updateExpr)
      depth--
    }
    addIndent()
    sb.append("BODY:\n")
    depth++
    visitStmt(stmt.body)
    depth--
    depth--
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Variable -> visitIdentifier(expr)
    is Literal -> visitLiteral(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
    is Assignment -> visitAssignment(expr)
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

  private fun visitIdentifier(expr: Variable) {
    addIndent()
    sb.append("Access Variable: ${expr.variable}\n")
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

  private fun visitAssignment(expr: Assignment) {
    addIndent()
    sb.append("ASSIGN:\n")
    depth++
    visitExpr(expr.left)
    visitExpr(expr.right)
    depth--
  }

  private fun addIndent() {
    sb.append(" ".repeat(n = depth * 2))
  }
}
