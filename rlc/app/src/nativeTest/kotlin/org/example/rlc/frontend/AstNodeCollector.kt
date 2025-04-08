@file:OptIn(ExperimentalUuidApi::class)

package org.example.rlc.frontend

import org.example.rlc.frontend.ast.Ast
import org.example.rlc.frontend.ast.BlockStmt
import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.ForStmt
import org.example.rlc.frontend.ast.FunDeclStmt
import org.example.rlc.frontend.ast.IfStmt
import org.example.rlc.frontend.ast.PrintStmt
import org.example.rlc.frontend.ast.ReturnStmt
import org.example.rlc.frontend.ast.Stmt
import org.example.rlc.frontend.ast.VarDeclStmt
import org.example.rlc.frontend.ast.WhileStmt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AstNodeCollector {
  private val statements = mutableMapOf<Uuid, Stmt>()
//  private val expressions = mutableMapOf<Uuid, Expr>()

  fun collect(roots: Ast) {
    roots.forEach(this::visitStmt)
  }

  fun getStatements(): Map<Uuid, Stmt> {
    return statements.toMap()
  }

//  fun getExpressions(): Map<Uuid, Expr> {
//    return mapOf()
//  }

  private fun visitStmt(stmt: Stmt) {
    statements[stmt.uid] = stmt

    when (stmt) {
      is BlockStmt -> visitBlockStmt(stmt)
      is ExprStmt -> {}
      is ForStmt -> visitForStmt(stmt)
      is FunDeclStmt -> visitFunDeclStmt(stmt)
      is IfStmt -> visitIfStmt(stmt)
      is PrintStmt -> {}
      is ReturnStmt -> {}
      is VarDeclStmt -> {}
      is WhileStmt -> visitWhileStmt(stmt)
    }
  }

  private fun visitBlockStmt(block: BlockStmt) {
    block.statements.forEach(this::visitStmt)
  }

//  private fun visitExprStmt() {
//    visitExpr()
//  }

  private fun visitForStmt(forStmt: ForStmt) {
    forStmt.initStmt?.let(this::visitStmt)
//    forStmt.conditionExpr?.let(this::visitExpr)
//    forStmt.updateExpr?.let(this::visitExpr)
    visitStmt(forStmt.body)
  }

  private fun visitFunDeclStmt(funDeclStmt: FunDeclStmt) {
    visitStmt(funDeclStmt.body)
  }

  private fun visitIfStmt(ifStmt: IfStmt) {
    visitStmt(ifStmt.ifBranch)
    ifStmt.elseBranch?.let(this::visitStmt)
  }

  private fun visitWhileStmt(whileStmt: WhileStmt) {
    visitStmt(whileStmt.body)
  }

//  private fun

//  private fun visitExpr() {
////    expressions[expr.uid] = expr
////
////    when (expr) {
////      is Assignment -> {}
////      is Binary -> {}
////      is CallExpr -> {}
////      is Grouping -> {}
////      is Literal -> {}
////      is Logical -> {}
////      is Unary -> {}
////      is Variable -> {}
////    }
//  }
}
