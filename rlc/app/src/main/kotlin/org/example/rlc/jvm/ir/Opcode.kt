package org.example.rlc.jvm.ir

enum class Opcode(val value: Byte) {
  // Opcodes stored alphabetically by mnemonics
  OP_ALOAD_0(0x2A.toByte()),  // Load reference from local variable 0
  OP_ALOAD_1(0x2B.toByte()),  // Load reference from local variable 1

  // Return reference from method
  OP_ARETURN(0xB0.toByte()),

  // Check whether object is of given type
  // Takes two bytes as an operand
  OP_CHECKCAST(0xC0.toByte()),

  OP_DADD(0x63.toByte()), // Add two doubles
  OP_DDIV(0x6F.toByte()), // Divide double

  // Load double from local variable
  OP_DLOAD_0(0x26.toByte()),

  OP_DMUL(0x6B.toByte()), // Multiply double
  OP_DNEG(0x77.toByte()), // Negate double
  OP_DSUB(0x67.toByte()), // Subtract double

  // Duplicate the top operand stack value
  OP_DUP(0x59.toByte()),

  OP_GETFIELD(0xB4.toByte()),  // Fetch field from object

  // Get static field from class
  // Takes two bytes as an operand
  OP_GETSTATIC(0xB2.toByte()),

  OP_ICONST_0(0x3.toByte()),  // Push int constant
  OP_IFEQ(0x99.toByte()),  // Branch if int comparison with zero succeeds

  // Invoke instance method;
  // direct invocation of instance initialization methods;
  // Takes two bytes as an operand
  OP_INVOKE_SPECIAL(0xB7.toByte()),

  // Invoke a class (static) method
  // Takes two bytes as an operand
  OP_INVOKE_STATIC(0xB8.toByte()),

  // Invoke instance method; dispatch based on class,
  // Takes two bytes as an operand
  OP_INVOKE_VIRTUAL(0xB6.toByte()),

  OP_INSTANCEOF(0xC1.toByte()), // Determine if object is of given type
  OP_IRETURN(0xAC.toByte()),   // Return int from method

  // Push item from run-time constant pool
  // Takes one byte as an operand
  OP_LDC(0x12.toByte()),

  // Create new object
  // Takes two bytes as an operand
  OP_NEW(0xBB.toByte()),

  // Set field in object
  // Takes two bytes as an operand
  OP_PUTFIELD(0xB5.toByte()),

  // Set static field in class
  // Takes two bytes as an operand
  OP_PUTSTATIC(0xB3.toByte()),

  // Return void fromm method
  // Takes no bytes as an operand
  OP_RETURN(0xB1.toByte()),
}
