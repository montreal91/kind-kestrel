package org.example.rlc.jvm.ir

enum class Opcode(val value: Byte) {
  // Opcodes stored alphabetically by mnemonics
  OP_ALOAD_0(0x2a.toByte()),  // Load reference from local variable 0
  OP_ALOAD_1(0x2b.toByte()),  // Load reference from local variable 1
  OP_DADD(0x63.toByte()), // Add two doubles
  OP_DDIV(0x6f.toByte()), // Divide double
  OP_DMUL(0x6b.toByte()), // Multiply double
  OP_DNEG(0x77.toByte()), // Negate double
  OP_DSUB(0x67.toByte()), // Subtract double
  OP_GET_STATIC(0xb2.toByte()),  // Get static field from class
  OP_INVOKE_SPECIAL(0xb7.toByte()),  // Invoke instance method; direct invocation of instance initialization methods
  OP_INVOKE_VIRTUAL(0xb6.toByte()),  // Invoke instance method; dispatch based on class
  OP_LDC(0x12.toByte()),  // Push item from run-time constant pool
  OP_PUTFIELD(0xb5.toByte()), // Set field in object
  OP_RETURN(0xb1.toByte()),  // Return void fromm method
}
