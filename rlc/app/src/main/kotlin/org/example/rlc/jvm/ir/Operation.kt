package org.example.rlc.jvm.ir

class Operation(val opcode: Opcode, val operands: List<ConstantPoolInfo>) {
  constructor(opcode: Opcode) : this(opcode, listOf())
}
