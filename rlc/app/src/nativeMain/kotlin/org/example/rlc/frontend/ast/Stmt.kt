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
class VarDeclStmt(val identifier: String, val token: Token, val initializer: Expr?): Stmt()

typealias Ast = List<Stmt>
