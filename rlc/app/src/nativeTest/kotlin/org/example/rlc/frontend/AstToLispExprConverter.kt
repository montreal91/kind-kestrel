package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Assign
import org.example.rlc.frontend.ast.Binary
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.Call
import org.example.rlc.frontend.ast.ClassDeclStmt
import org.example.rlc.frontend.ast.Expr
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.Get
import org.example.rlc.frontend.ast.Grouping
import org.example.rlc.frontend.ast.IfStmt
import org.example.rlc.frontend.ast.Literal
import org.example.rlc.frontend.ast.Logical
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.ReturnStmt
import org.example.rlc.frontend.ast.Set
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.This
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

    ast.forEach(action = this::visitStmt)

    sb.append(")")
    return sb.toString()
  }

  private fun visitStmt(stmt: Stmt) {
    when (stmt) {
      is BlockStmt -> visitBlockStmt(block = stmt, isBody = false)
      is ExprStmt -> visitExprStmt(stmt)
      is ForStmt -> TODO()
      is FunDeclStmt -> visitFunDeclStmt(funDecl = stmt, isMethod = false)
      is IfStmt -> TODO()
      is PrintStmt -> visitPrintStmt(stmt)
      is ReturnStmt -> visitReturnStmt(stmt)
      is VarDeclStmt -> visitVarDeclStmt(stmt)
      is WhileStmt -> TODO()
      is ClassDeclStmt -> visitClassDeclStmt(stmt)
    }
  }

  private fun visitExpr(expr: Expr) {
    when (expr) {
      is Assign -> TODO()
      is Binary -> visitBinary(expr)
      is Call -> visitCall(expr)
      is Grouping -> visitGrouping(expr)
      is Literal -> visitLiteral(expr)
      is Logical -> visitLogical(expr)
      is Unary -> visitUnary(expr)
      is Variable -> visitVariable(expr)
      is Get -> visitGet(get = expr)
      is Set -> visitSet(set = expr)
      is This -> visitThis()
    }
  }

  private fun visitBlockStmt(block: BlockStmt, isBody: Boolean) {
    val header = when (isBody) {
      true -> "body"
      false -> "block"
    }

    if (!isBody) {
      addIndent()
    }

    sb.append("($header\n")
    depth++

    block.statements.forEach {
      visitStmt(stmt=it)
    }

    depth--
    addIndent()
    sb.append(")")

    if (!isBody) {
      sb.append("\n")
    }
  }

  private fun visitExprStmt(stmt: ExprStmt) {
    addIndent()
    sb.append("(expr ")
    visitExpr(expr = stmt.expr)
    sb.append(")\n")
  }

  private fun visitFunDeclStmt(funDecl: FunDeclStmt, isMethod: Boolean) {
    addIndent()

    val header = when(isMethod) {
      true -> "method"
      false -> "fun"
    }

    sb.append("($header ${funDecl.identifier.value} (parameters")

    if (funDecl.parameters.isNotEmpty()) {
      sb.append(" ")
    }

    sb.append(funDecl.parameters.joinToString(separator = " ") { it.identifier.value })
    sb.append(") ")

    visitBlockStmt(block = funDecl.body, isBody = true)

    sb.append(")\n")
  }

  private fun visitPrintStmt(stmt: PrintStmt) {
    addIndent()
    sb.append("(print ")
    visitExpr(expr = stmt.expr)
    sb.append(")\n")
  }

  private fun visitReturnStmt(stmt: ReturnStmt) {
    addIndent()
    sb.append("(return ")

    when (val expr = stmt.expr) {
      null -> sb.append("nil")
      else -> visitExpr(expr)
    }

    sb.append(")\n")
  }

  private fun visitVarDeclStmt(stmt: VarDeclStmt) {
    addIndent()
    sb.append("(var ${stmt.variable} ")

    when (val initializer = stmt.initializer) {
      null -> sb.append("nil")
      else -> visitExpr(expr = initializer)
    }

    sb.append(")\n")
  }

  private fun visitClassDeclStmt(stmt: ClassDeclStmt) {
    addIndent()
    sb.append("(class ${stmt.identifier.value}")

    when (stmt.superclass == null) {
      true -> {}
      false -> {
        sb.addSpaceIfNeeded()
        sb.append("(superclass ${stmt.superclass.value})")
      }
    }

    if (stmt.methods.isEmpty()) {
      sb.append(")\n")
      return
    }

    sb.append("\n")

    depth++

    for (meth in stmt.methods) {
      visitFunDeclStmt(funDecl = meth, isMethod = true)
    }

    depth--
    addIndent()
    sb.append(")\n")
  }

  private fun visitBinary(expr: Binary) {
    sb.append("(${expr.operator.value} ")
    visitExpr(expr = expr.left)
    visitExpr(expr = expr.right)
    sb.append(")")
  }

  private fun visitCall(expr: Call) {
    sb.append("(call ")
    visitExpr(expr = expr.callee)
    sb.addSpaceIfNeeded()

    sb.append("(")

    expr.args.forEach {
      visitExpr(expr = it)
      sb.addSpaceIfNeeded()
    }

    sb.append("))")
  }

  private fun visitGet(get: Get) {
    sb.append("(get ")
    visitExpr(expr = get.obj)
    sb.addSpaceIfNeeded()
    sb.append("${get.name})")
  }

  private fun visitGrouping(expr: Grouping) {
    sb.append("(group ")
    visitExpr(expr = expr.expression)
    sb.append(")")
  }

  private fun visitLiteral(expr: Literal) {
    sb.addSpaceIfNeeded()
    when (expr.type) {
      Literal.Type.STRING -> sb.append("\"${expr.value}\"")
      else -> sb.append(expr.value)
    }
  }

  private fun visitLogical(expr: Logical) {
    sb.append("(${expr.operator.value} ")
    visitExpr(expr = expr.left)
    visitExpr(expr = expr.right)
    sb.append(")")
  }

  private fun visitSet(set: Set) {
    sb.append("(set ")
    visitExpr(expr = set.obj)
    sb.addSpaceIfNeeded()
    sb.append("${set.name} ")
    visitExpr(expr = set.value)
    sb.append(")")
  }

  private fun visitThis() {
    sb.append("(this)")
  }

  private fun visitUnary(expr: Unary) {
    sb.append("(${expr.operator.value}")
    visitExpr(expr = expr.right)
    sb.append(")")
  }

  private fun visitVariable(expr: Variable) {
    sb.append("(variable ${expr.variable})")
  }


  private fun addIndent() {
    sb.append(" ".repeat(n = depth * 2))
  }
}
