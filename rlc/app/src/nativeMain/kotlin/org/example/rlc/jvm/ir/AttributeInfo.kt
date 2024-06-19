package org.example.rlc.jvm.ir

import kotlin.math.max

sealed class AttributeInfo(val attributeName: Utf8Value)


sealed class StackMapFrame(val tag: Byte) {
  abstract fun toBytes(): List<Byte>
}

class SameFrame(tag: Byte) : StackMapFrame(tag) {
  override fun toBytes(): List<Byte> {
    return listOf(tag)
  }
}

class StackMapTableAttribute(
  val frames: List<StackMapFrame>
) : AttributeInfo(stackMapTableAttributeName) {
  fun isEmpty() = frames.isEmpty()
}

class CodeAttribute(
  private val argsSize: Int,
  val code: List<Operation>,
  val exceptionTable: Any, // Replace with actual type later
  private val attributes: List<AttributeInfo>,
): AttributeInfo(codeAttributeName) {
  private val computedAttributes = mutableListOf<AttributeInfo>()
  private val _maxLocals: Int
  private val _maxStack: Int

  init {
    _maxLocals = calculateMaxLocals()
    _maxStack = calculateMaxStack()
  }

  constructor(argsSize: Int, code: List<Operation>) : this(
    argsSize = argsSize,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  val allAttributes: List<AttributeInfo> get() = attributes + computedAttributes
  val maxLocals: Short get() = max(argsSize, _maxLocals).toShort()
  val maxStack: Short get() = _maxStack.toShort()

  fun addAttribute(attribute: AttributeInfo) = computedAttributes.add(attribute)

  private fun calculateMaxLocals(): Int {
    var res = 0

    for (operation in code) {
      res = max(res, opToLocalRef(operation.opcode))
    }

    return res
  }

  private fun calculateMaxStack(): Int {
    // This algorithm is dummy, but working and should be improved later.
    var res = 0
    var stackSize = 0

    for (operation in code) {
      stackSize += operation.stackModification
      stackSize = max(a = stackSize, b = 0)
      res = max(stackSize, res)
    }

    return res
  }

  private fun opToLocalRef(operation: Opcode) = when (operation) {
    Opcode.OP_ALOAD_0 -> 1
    Opcode.OP_ALOAD_1 -> 2
    Opcode.OP_ALOAD_2 -> 3
    Opcode.OP_ALOAD_3 -> 4
    Opcode.OP_ARETURN -> 0
    Opcode.OP_ASTORE_2 -> 3
    Opcode.OP_ASTORE_3 -> 4
    Opcode.OP_ATHROW -> 0
    Opcode.OP_CHECKCAST -> 0
    Opcode.OP_DADD -> 0
    Opcode.OP_DDIV -> 0
    Opcode.OP_DLOAD_0 -> 2
    Opcode.OP_DLOAD_1 -> 3
    Opcode.OP_DMUL -> 0
    Opcode.OP_DNEG -> 0
    Opcode.OP_DSUB -> 0
    Opcode.OP_DUP -> 0
    Opcode.OP_GETFIELD -> 0
    Opcode.OP_GETSTATIC -> 0
    Opcode.OP_IAND -> 0
    Opcode.OP_ICONST_0 -> 0
    Opcode.OP_ICONST_1 -> 0
    Opcode.OP_IFEQ -> 0
    Opcode.OP_IFNE -> 0
    Opcode.OP_ILOAD_1 -> 2
    Opcode.OP_INVOKE_SPECIAL -> 0
    Opcode.OP_INVOKE_STATIC -> 0
    Opcode.OP_INVOKE_VIRTUAL -> 0
    Opcode.OP_INSTANCEOF -> 0
    Opcode.OP_IRETURN -> 0
    Opcode.OP_IXOR -> 0
    Opcode.OP_LDC -> 0
    Opcode.OP_LDC2_W -> 0
    Opcode.OP_NEW -> 0
    Opcode.OP_POP -> 0
    Opcode.OP_PUTFIELD -> 0
    Opcode.OP_PUTSTATIC -> 0
    Opcode.OP_RETURN -> 0
  }
}
