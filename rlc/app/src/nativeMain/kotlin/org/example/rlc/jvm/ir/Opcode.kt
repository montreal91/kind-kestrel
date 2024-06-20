package org.example.rlc.jvm.ir

enum class Opcode(val value: Byte) {
  // Opcodes stored alphabetically by mnemonics
  OP_ALOAD_0(0x2A.toByte()),  // Load reference from local variable 0
  OP_ALOAD_1(0x2B.toByte()),  // Load reference from local variable 1

  // Load reference from local variable 2
  OP_ALOAD_2(0x2C.toByte()),

  // Load reference from local variable 3
  OP_ALOAD_3(0x2D.toByte()),

  // Return reference from method
  OP_ARETURN(0xB0.toByte()),

  // Store reference into local variable
  OP_ASTORE_2(0x4D.toByte()),

  // Store reference into local variable
  OP_ASTORE_3(0x4E.toByte()),

  // Throw exception or error
  OP_ATHROW(0xBF.toByte()),

  // Check whether object is of given type
  // Takes two bytes as an operand
  OP_CHECKCAST(0xC0.toByte()),

  OP_DADD(0x63.toByte()), // Add two doubles

  // Compare double values on operand stack
  // Pushes int value as a result
  OP_DCMPG(0x98.toByte()),

  OP_DDIV(0x6F.toByte()), // Divide double

  // Load double from local variable
  OP_DLOAD_0(0x26.toByte()),

  // Load double from local variable
  OP_DLOAD_1(0x27.toByte()),

  OP_DMUL(0x6B.toByte()), // Multiply double
  OP_DNEG(0x77.toByte()), // Negate double
  OP_DSUB(0x67.toByte()), // Subtract double

  // Duplicate the top operand stack value
  OP_DUP(0x59.toByte()),

  // Fetch field from object.
  // Pops object reference from the stack
  // Pushes field value to the stack
  OP_GETFIELD(0xB4.toByte()),

  // Get static field from class
  // Takes two bytes as an operand
  OP_GETSTATIC(0xB2.toByte()),

  // Push int constant 0
  OP_ICONST_0(0x3.toByte()),

  // Bitwise AND of int values
  // Pops two int values from the stack
  // Pushes the result to the stack
  OP_IAND(0x7E.toByte()),

  // Push int constant 1
  OP_ICONST_1(0x4.toByte()),

  // Branch if int comparison with zero succeeds.
  // Succeeds if and only if value = 0
  OP_IFEQ(0x99.toByte()),

  // Branch if int comparison with zero succeeds.
  // Succeeds if and only if value != 0
  OP_IFNE(0x9A.toByte()),

  // Load int from local variable
  OP_ILOAD_1(0x1B.toByte()),

  // Determine if object is of given type
  // Takes two bytes as an operand
  OP_INSTANCEOF(0xC1.toByte()),

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

  // Return int from method
  OP_IRETURN(0xAC.toByte()),

  // Boolean XOR int
  // Pops two int values from the stack
  // Pushes result to the stack
  OP_IXOR(0x82.toByte()),

  // Push item from run-time constant pool
  // Takes one byte as an operand
  OP_LDC(0x12.toByte()),

  // Push long or double from run-time constant pool (wide index)
  // Takes two bytes as an operand
  OP_LDC2_W(0x14.toByte()),

  // Create new object
  // Takes two bytes as an operand
  OP_NEW(0xBB.toByte()),

  // Pop the top operand stack value
  OP_POP(0x57.toByte()),

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
