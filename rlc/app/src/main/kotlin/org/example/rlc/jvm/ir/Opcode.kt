package org.example.rlc.jvm.ir

enum class Opcode(val value: Byte) {
  // Opcodes stored alphabetically by mnemonics
  OP_ALOAD_0(0x2a.toByte()),  // Load reference from local variable 0
  OP_GET_STATIC(0xb2.toByte()),  // Get static field from class
  OP_INVOKE_SPECIAL(0xb7.toByte()),  // Invoke instance method; direct invocation of instance initialization methods
  OP_INVOKE_VIRTUAL(0xb6.toByte()),  // Invoke instance method; dispatch based on class
  OP_LDC(0x12.toByte()),  // Push item from run-time constant pool
  OP_RETURN(0xb1.toByte()),  // Return void fromm method
}
