@file:OptIn(ExperimentalUuidApi::class)

package org.example.rlc.frontend.ast

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.example.rlc.frontend.Token
import org.example.rlc.frontend.scope.VariableResolutionResult

sealed class Stmt(val uid: Uuid)

class ExprStmt(uid: Uuid, val expr: Expr): Stmt(uid)
class PrintStmt(uid: Uuid, val expr: Expr): Stmt(uid)
class BlockStmt(uid: Uuid, val statements: List<Stmt>): Stmt(uid)
class VarDeclStmt(uid: Uuid, val variable: String, val token: Token, val initializer: Expr?): Stmt(uid)
class IfStmt(uid: Uuid, val expr: Expr, val ifBranch: Stmt, val elseBranch: Stmt?): Stmt(uid)
class WhileStmt(uid: Uuid,val expr: Expr, val body: Stmt): Stmt(uid)

class ForStmt(
  uid: Uuid,
  val initStmt: Stmt?,
  val conditionExpr: Expr?,
  val updateExpr: Expr?,
  val body: Stmt
): Stmt(uid)

class FunDeclStmt(
  uid: Uuid,
  val identifier: Token,
  val parameters: Array<FormalParameter>,
  val body: BlockStmt
): Stmt(uid) {
  val arity: Int get() = parameters.size
  val enclosedVariables = mutableListOf<VariableResolutionResult>()

  private var isClosure = false

  fun markAsClosure() {
    isClosure = true
  }

  fun isClosure(): Boolean {
    return isClosure
  }
}

class FormalParameter(
  val uid: Uuid,
  val identifier: Token
)

class ReturnStmt(uid: Uuid, val expr: Expr?): Stmt(uid)

typealias Ast = List<Stmt>
