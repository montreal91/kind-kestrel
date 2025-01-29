package org.example.rlc.frontend.ast

import org.example.rlc.frontend.Token
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class Expr {
  val uid = Uuid.random()
}

class Literal(val value: String, val type: Type) : Expr() {
  enum class Type {
    NUMBER,
    BOOLEAN,
    STRING,
    NIL_TYPE,
  }
}

fun tokenTypeToLiteralType(tokenType: Token.Type) = when (tokenType) {
  Token.Type.NUMBER -> Literal.Type.NUMBER
  Token.Type.STRING -> Literal.Type.STRING
  Token.Type.TRUE -> Literal.Type.BOOLEAN
  Token.Type.FALSE -> Literal.Type.BOOLEAN
  Token.Type.NIL -> Literal.Type.NIL_TYPE
  else -> error("Token $tokenType does not represent a valid literal.")
}

class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()
class Unary(val operator: Token, val right: Expr) : Expr()
class Grouping(val expression: Expr) : Expr()
class Identifier(val identifier: String) : Expr()
