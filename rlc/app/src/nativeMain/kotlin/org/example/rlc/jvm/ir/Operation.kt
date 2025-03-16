package org.example.rlc.jvm.ir

sealed class Operation(val opcode: Opcode, val valueInfo: VerificationTypeInfo?) {
  init {
    when (opcode) {
      Opcode.OP_ASTORE_2 -> checkValueNotNull()
      Opcode.OP_ASTORE_3 -> checkValueNotNull()
      Opcode.OP_GETFIELD -> checkValueNotNull()
      Opcode.OP_GETSTATIC -> checkValueNotNull()
      Opcode.OP_LDC -> checkValueNotNull()
      Opcode.OP_LDC2_W -> checkValueNotNull()
      Opcode.OP_NEW -> checkValueNotNull()
      else -> {}
    }
  }

  val stackModification: Int
    get() = when (opcode) {
      Opcode.OP_ACONST_NULL -> 1
      Opcode.OP_ALOAD_0 -> 1
      Opcode.OP_ALOAD_1 -> 1
      Opcode.OP_ALOAD_2 -> 1
      Opcode.OP_ALOAD_3 -> 1
      Opcode.OP_ARETURN -> -1
      Opcode.OP_ASTORE_2 -> -1
      Opcode.OP_ASTORE_3 -> -1
      Opcode.OP_ATHROW -> -1
      Opcode.OP_DADD -> -2
      Opcode.OP_DCMPG -> -3
      Opcode.OP_DDIV -> -2
      Opcode.OP_DLOAD_0 -> 2
      Opcode.OP_DLOAD_1 -> 2
      Opcode.OP_DMUL -> -2
      Opcode.OP_DNEG -> -2
      Opcode.OP_DSUB -> -2
      Opcode.OP_DUP -> 1
      Opcode.OP_IADD -> -1
      Opcode.OP_IAND -> -1
      Opcode.OP_ICONST_0 -> 1
      Opcode.OP_ICONST_1 -> 1
      Opcode.OP_ICONST_2 -> 1
      Opcode.OP_ICONST_3 -> 1
      Opcode.OP_ILOAD_1 -> 1
      Opcode.OP_IRETURN -> -1
      Opcode.OP_ISHR -> -1
      Opcode.OP_IXOR -> -1
      Opcode.OP_POP -> -1
      Opcode.OP_RETURN -> 0
      Opcode.OP_IFEQ -> -1
      Opcode.OP_IFNE -> -1
      Opcode.OP_LDC -> 1
      Opcode.OP_CHECKCAST -> 0
      Opcode.OP_GETFIELD -> customStackModification()
      Opcode.OP_GETSTATIC -> customStackModification()
      Opcode.OP_INVOKE_SPECIAL -> customStackModification()
      Opcode.OP_INVOKE_STATIC -> customStackModification()
      Opcode.OP_INVOKE_VIRTUAL -> customStackModification()
      Opcode.OP_INSTANCEOF -> 0
      Opcode.OP_LDC2_W -> 2
      Opcode.OP_NEW -> 1
      Opcode.OP_PUTFIELD -> customStackModification()
      Opcode.OP_PUTSTATIC -> customStackModification()
      Opcode.OP_GOTO -> 0
      Opcode.OP_ALOAD -> 1
      Opcode.OP_ASTORE -> -1
      Opcode.OP_IF_ICMPNE -> -2
    }

  protected open fun customStackModification() = 0

  private fun checkValueNotNull() {
    if (valueInfo == null) {
      throw IllegalStateException("Operation with opcode $opcode should have non-null valueInfo.")
    }
  }
}

private fun calculateStackModification(constant: ConstantPoolInfo) = when (constant) {
  is FieldRefInfo -> constant.size
  is MethodRefInfo -> constant.returnSize - constant.argsSize
  else -> 0
}

class SimpleOperation(
  opcode: Opcode,
  value: VerificationTypeInfo?
) : Operation(opcode, valueInfo = value) {
  constructor(opcode: Opcode) : this(opcode, value = null)
}

class ControlFlowOperation(opcode: Opcode, private var jumpTo: Int) : Operation(opcode, valueInfo = null) {
  fun setJumpTo(offset: Int) {
    jumpTo = offset
  }

  fun getJumpTo() = jumpTo
}

class ByteConstantOperation(
  opcode: Opcode,
  val constant: ConstantPoolInfo,
  value: VerificationTypeInfo?
) : Operation(opcode, valueInfo = value) {
  constructor(opcode: Opcode, constant: ConstantPoolInfo) : this(opcode, constant, value = null)
}

class OperationWithIndex(
  opcode: Opcode,
  val index: Byte,
  value: VerificationTypeInfo
) : Operation(opcode, valueInfo = value)

class ShortConstantOperation(
  opcode: Opcode,
  val constant: ConstantPoolInfo,
  value: VerificationTypeInfo?
) : Operation(opcode, valueInfo = value) {
  constructor(opcode: Opcode, constant: ConstantPoolInfo) : this(opcode, constant, value = null)

  override fun customStackModification() = when (opcode) {
    Opcode.OP_GETFIELD -> calculateStackModification(constant)
    Opcode.OP_GETSTATIC -> calculateStackModification(constant)
    Opcode.OP_INVOKE_SPECIAL -> calculateStackModification(constant)
    Opcode.OP_INVOKE_STATIC -> calculateStackModification(constant)
    Opcode.OP_INVOKE_VIRTUAL -> calculateStackModification(constant)
    Opcode.OP_PUTFIELD -> -1 - calculateStackModification(constant)
    Opcode.OP_PUTSTATIC -> -1 - calculateStackModification(constant)
    else -> super.customStackModification()
  }
}
