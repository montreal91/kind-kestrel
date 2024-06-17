package org.example.rlc.jvm.ir

sealed class Operation(val opcode: Opcode) {
  abstract val stackModification: Int
}

class SimpleOperation(opcode: Opcode) : Operation(opcode) {
  override val stackModification: Int
    get() = when (opcode) {
      Opcode.OP_ALOAD_0 -> 1
      Opcode.OP_ALOAD_1 -> 1
      Opcode.OP_ALOAD_2 -> 1
      Opcode.OP_ALOAD_3 -> 1
      Opcode.OP_ARETURN -> -1
      Opcode.OP_ASTORE_2 -> -1
      Opcode.OP_ASTORE_3 -> -1
      Opcode.OP_ATHROW -> -1
      Opcode.OP_DADD -> -2
      Opcode.OP_DDIV -> -2
      Opcode.OP_DLOAD_0 -> 2
      Opcode.OP_DLOAD_1 -> 2
      Opcode.OP_DMUL -> -2
      Opcode.OP_DNEG -> -2
      Opcode.OP_DSUB -> -2
      Opcode.OP_DUP -> 1
      Opcode.OP_ICONST_0 -> 1
      Opcode.OP_ICONST_1 -> 1
      Opcode.OP_ILOAD_1 -> 1
      Opcode.OP_IRETURN -> -1
      Opcode.OP_RETURN -> 0
      else -> throw IllegalArgumentException(
        "Invalid opcode for Simple Operation: $opcode"
      )
    }
}

class ControlFlowOperation(opcode: Opcode, val jumpTo: Int) : Operation(opcode) {
  override val stackModification: Int
    get() = when(opcode) {
      Opcode.OP_IFEQ -> -1
      else -> throw IllegalArgumentException(
        "Invalid opcode for Control Flow Operation: $opcode"
      )
    }
}

class ByteConstantOperation(opcode: Opcode, val constant: ConstantPoolInfo) : Operation(opcode) {
  override val stackModification: Int
    get() = when(opcode) {
      Opcode.OP_LDC -> 1
      else -> throw IllegalArgumentException(
        "Invalid opcode for ByteConstant Operation: $opcode"
      )
    }
}

class ShortConstantOperation(opcode: Opcode, val constant: ConstantPoolInfo) : Operation(opcode) {
  override val stackModification: Int
    get() = when(opcode) {
      Opcode.OP_CHECKCAST -> 0
      Opcode.OP_GETFIELD -> calculateStackModification(constant)
      Opcode.OP_GETSTATIC -> calculateStackModification(constant)
      Opcode.OP_INVOKE_SPECIAL -> calculateStackModification(constant)
      Opcode.OP_INVOKE_STATIC -> calculateStackModification(constant)
      Opcode.OP_INVOKE_VIRTUAL -> calculateStackModification(constant)
      Opcode.OP_INSTANCEOF -> 0
      Opcode.OP_IRETURN -> -1
      Opcode.OP_LDC2_W -> 2
      Opcode.OP_NEW -> 1
      Opcode.OP_PUTFIELD -> -1 - calculateStackModification(constant)
      Opcode.OP_PUTSTATIC -> -1 - calculateStackModification(constant)
      else -> throw IllegalArgumentException(
        "Invalid opcode for ShortConstant Operation: $opcode"
      )
    }

  private fun calculateStackModification(constant: ConstantPoolInfo) = when (constant) {
    is FieldRefInfo -> constant.size
    is MethodRefInfo -> constant.returnSize - constant.argsSize
    else -> 0
  }
}
