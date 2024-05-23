package org.example.rlc.frontend

class LoxCompileError(val message: String, val token: Token) {
  override fun toString() = when (token.type) {
    Token.Type.EOF -> "[line ${token.lineNumber}] Error at end. : $message"
    else -> "[line ${token.lineNumber}] Error at '${token.value}': $message"
  }
}
