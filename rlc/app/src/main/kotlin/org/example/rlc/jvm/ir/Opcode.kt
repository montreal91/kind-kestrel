package org.example.rlc.jvm.ir

enum class Opcode(val value: Byte) {
  // Opcodes stored alphabetically by mnemonics
  OP_ALOAD_0(0x2A.toByte()),  // Load reference from local variable 0
  OP_ALOAD_1(0x2B.toByte()),  // Load reference from local variable 1
  OP_CHECKCAST(0xC0.toByte()),  // Check whether object is of given type
  OP_DADD(0x63.toByte()), // Add two doubles
  OP_DDIV(0x6F.toByte()), // Divide double
  OP_DMUL(0x6B.toByte()), // Multiply double
  OP_DNEG(0x77.toByte()), // Negate double
  OP_DSUB(0x67.toByte()), // Subtract double
  OP_GETFIELD(0xB4.toByte()),  // Fetch field from object
  OP_GETSTATIC(0xB2.toByte()),  // Get static field from class
  OP_ICONST_0(0x3.toByte()),  // Push int constant
  OP_IFEQ(0x99.toByte()),  // Branch if int comparison with zero succeeds
  OP_INVOKE_SPECIAL(0xB7.toByte()),  // Invoke instance method; direct invocation of instance initialization methods
  OP_INVOKE_VIRTUAL(0xB6.toByte()),  // Invoke instance method; dispatch based on class
  OP_INSTANCEOF(0xC1.toByte()), // Determine if object is of given type
  OP_IRETURN(0xAC.toByte()),   // Return int from method
  OP_LDC(0x12.toByte()),  // Push item from run-time constant pool
  OP_PUTFIELD(0xB5.toByte()), // Set field in object
  OP_RETURN(0xB1.toByte()),  // Return void fromm method
}
