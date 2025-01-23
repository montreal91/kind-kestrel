package org.example.rlc.frontend

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
import org.example.rlc.frontend.scope.LocalVariable
import org.example.rlc.frontend.scope.UnresolvedVariable
import org.example.rlc.frontend.scope.VariableResolutionResult
import org.example.rlc.frontend.scope.VariableResolutionTable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class Resolver {
  private val resolutionTable = VariableResolutionTable()
  private val frameStack = FrameStack()
  private val errors = mutableListOf<LoxCompileError>()

  val hasErrors = errors.isNotEmpty()

  fun resolve(program: Ast): VariableResolutionTable {
    program.forEach(this::visitStmt)

    return resolutionTable
  }

  fun getErrors() = errors.toList()

  private fun visitStmt(stmt: Stmt) = when (stmt) {
    is BlockStmt -> visitBlockStmt(stmt)
    is ExprStmt -> visitExprStmt(stmt)
    is PrintStmt -> visitPrintStmt(stmt)
    is VarDeclStmt -> visitVarDeclStmt(stmt)
  }

  private fun visitExpr(expr: Expr) = when (expr) {
    is Literal -> visitLiteral()
    is Binary -> visitBinary(expr)
    is Grouping -> visitGrouping(expr)
    is Identifier -> visitIdentifier(expr)
    is Logical -> visitLogical(expr)
    is Unary -> visitUnary(expr)
  }

  private fun visitBlockStmt(stmt: BlockStmt) {
    frameStack.addNewFrame()
    stmt.statements.forEach(this::visitStmt)
    frameStack.popFrame()
  }

  private fun visitExprStmt(stmt: ExprStmt) {
    visitExpr(stmt.expr)
  }

  private fun visitPrintStmt(stmt: PrintStmt) {
    visitExpr(stmt.expr)
  }

  private fun visitVarDeclStmt(stmt: VarDeclStmt) {
    if (frameStack.existInCurrentFrame(stmt.identifier)) {
      error(message = "This identifier already exists.", stmt.token)
    }

    frameStack.declareVariable(stmt.identifier)
    resolutionTable.set(stmt.uid, LocalVariable(frameStack.lookup(stmt.identifier)))
  }

  private fun error(message: String, token: Token) {
    errors.add(LoxCompileError(message = message, token))
  }

  private fun visitLiteral() {}

  private fun visitBinary(expr: Binary) {
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitGrouping(expr: Grouping) {
    visitExpr(expr.expression)
  }

  private fun visitIdentifier(expr: Identifier) {
    resolutionTable.set(expr.uid, resolveVariable(expr.identifier))
  }

  private fun visitLogical(expr: Logical) {
    visitExpr(expr.left)
    visitExpr(expr.right)
  }

  private fun visitUnary(expr: Unary) {
    visitExpr(expr.right)
  }

  private fun resolveVariable(identifier: String): VariableResolutionResult =
    if (frameStack.existInAllFrames(identifier)) {
      LocalVariable(frameStack.lookup(identifier))
    } else {
      UnresolvedVariable
    }
}
