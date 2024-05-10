package org.example.rlc.frontend

class LoxCompileError(val message: String, val lineNumber: Int) {
  override fun toString() = "[line $lineNumber] $message"
}
