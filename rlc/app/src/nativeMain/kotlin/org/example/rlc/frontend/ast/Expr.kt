@file:OptIn(ExperimentalUuidApi::class)

package org.example.rlc.frontend.ast

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.example.rlc.frontend.Token

sealed class Expr(val uid: Uuid) {
  open val canAssign = false
}

class Literal(uid: Uuid, val value: String, val type: Type) : Expr(uid) {
  enum class Type {
    NUMBER,
    BOOLEAN,
    STRING,
    NIL_TYPE,
  }
}

class Binary(uid: Uuid, val left: Expr, val operator: Token, val right: Expr) : Expr(uid)
class Logical(uid: Uuid, val left: Expr, val operator: Token, val right: Expr) : Expr(uid)
class Unary(uid: Uuid, val operator: Token, val right: Expr) : Expr(uid)
class Grouping(uid: Uuid, val expression: Expr) : Expr(uid)

class Variable(uid: Uuid, val variable: String) : Expr(uid) {
  override val canAssign = true

  override fun toString(): String {
    return "(Variable [${variable}])"

  }
}

class Get(uid: Uuid, val obj: Expr, val name: String) : Expr(uid) {
  override val canAssign = true
}

class Set(uid: Uuid, val obj: Expr, val name: String, val value: Expr) : Expr(uid)

class Assign(uid: Uuid, val left: Expr, val right: Expr) : Expr(uid)

class Call(uid: Uuid, val callee: Expr, val args: List<Expr>) : Expr(uid)

class This(uid: Uuid): Expr(uid)
