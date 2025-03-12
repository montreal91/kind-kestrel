package org.example.rlc.frontend.ast

import org.example.rlc.frontend.Token
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class Stmt {
  val uid = Uuid.random()
}

class ExprStmt(val expr: Expr): Stmt()
class PrintStmt(val expr: Expr): Stmt()
class BlockStmt(val statements: List<Stmt>): Stmt()
class VarDeclStmt(val variable: String, val token: Token, val initializer: Expr?): Stmt()
class IfStmt(val expr: Expr, val ifBranch: Stmt, val elseBranch: Stmt?): Stmt()
class WhileStmt(val expr: Expr, val body: Stmt): Stmt()

class ForStmt(
  val initStmt: Stmt?,
  val conditionExpr: Expr?,
  val updateExpr: Expr?,
  val body: Stmt
): Stmt()

class FunDeclStmt(
  val identifier: Token,
  val parameters: Array<Token>,
  val body: BlockStmt
): Stmt() {
  val arity: Int get() = parameters.size
}

class ReturnStmt(val expr: Expr?): Stmt()

typealias Ast = List<Stmt>
