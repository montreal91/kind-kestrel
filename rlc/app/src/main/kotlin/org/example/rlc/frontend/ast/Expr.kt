package org.example.rlc.frontend.ast

import org.example.rlc.frontend.Token

sealed class Expr

class Literal(val value: String, val type: Type) : Expr() {
  enum class Type {
    NUMBER,
    BOOLEAN,
    STRING,
  }
}

class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()
class Unary(val operator: Token, val right: Expr) : Expr()
class Grouping(val expression: Expr) : Expr()
