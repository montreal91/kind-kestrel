package org.example.rlc.frontend.ast

sealed class Stmt

class ExprStmt(val expr: Expr): Stmt()
class PrintStmt(val expr: Expr): Stmt()
