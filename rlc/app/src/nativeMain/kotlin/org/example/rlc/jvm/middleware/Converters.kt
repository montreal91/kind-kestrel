package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.ast.Literal
import org.example.rlc.jvm.ir.ConstantPoolInfo
import org.example.rlc.jvm.ir.toDoubleValue
import org.example.rlc.jvm.ir.toStringRefInfo

internal fun Literal.toConstant(): ConstantPoolInfo {
  return when (this.type) {
    Literal.Type.NUMBER -> this.value.toDouble().toDoubleValue()
    Literal.Type.BOOLEAN -> TODO()
    Literal.Type.STRING -> this.value.toStringRefInfo()
    Literal.Type.NIL_TYPE -> TODO()
  }
}
