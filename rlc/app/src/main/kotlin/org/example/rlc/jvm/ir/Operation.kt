package org.example.rlc.jvm.ir

sealed class Operation(val opcode: Opcode)

class SimpleOperation(opcode: Opcode) : Operation(opcode)
class ControlFlowOperation(opcode: Opcode, val jumpTo: Int) : Operation(opcode)
class ByteConstantOperation(opcode: Opcode, val constant: ConstantPoolInfo) : Operation(opcode)
class ShortConstantOperation(opcode: Opcode, val constant: ConstantPoolInfo) : Operation(opcode)
