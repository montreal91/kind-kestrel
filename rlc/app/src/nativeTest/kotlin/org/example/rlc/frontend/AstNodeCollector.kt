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

  fun collect(roots: Ast) {
    roots.forEach(this::visitStmt)
  }

  fun getStatements(): Map<Uuid, Stmt> {
    return statements.toMap()
  }

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

  private fun visitForStmt(forStmt: ForStmt) {
    forStmt.initStmt?.let(this::visitStmt)
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
}
