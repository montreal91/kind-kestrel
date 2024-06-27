package org.example.rlc.jvm.ir

import org.example.rlc.jvm.middleware.generateRuntimeError
import org.example.rlc.jvm.middleware.loxDoubleClassInfo
import org.example.rlc.jvm.middleware.loxRuntimeErrorClassInfo
import org.example.rlc.jvm.middleware.runtimeErrorConstructorRef
import kotlin.test.Test
import kotlin.test.assertEquals

data class TestCase(val input: CodeAttribute, val expected: Short)

private fun addMethodCodeAttribute(): CodeAttribute {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.__add__:(LLoxDouble;)LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__add__:(LLoxDouble;)LLoxDouble;",
      name = "__add__".toUtf8Value(),
      descriptor = "(LLoxDouble;)LLoxDouble;".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 16),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 16),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_2),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_3),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_3),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, methodRefInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = "Both operands should be double.")),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  return CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
  )
}

private fun staticMethodNegate(): CodeAttribute {
  val methodName = "__neg__"
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.${methodName}:()LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "${methodName}:()LLoxDouble;",
      name = methodName.toUtf8Value(),
      descriptor = "()LLoxDouble;".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 7),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, methodRefInfo),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  return CodeAttribute(
    argsSize = 1,
    code = code + generateRuntimeError("Operand should be a number."),
    exceptionTable = 0,
    attributes = listOf(),
  )
}


class CodeAttributeTest {
  private val testCases = listOf(
    TestCase(addMethodCodeAttribute(), expected = 3),
    TestCase(staticMethodNegate(), expected = 3),
  )

  @Test
  fun test() {
    testCases.forEach(this::check)
  }

  private fun check(case: TestCase) {
    assertEquals(case.expected, case.input.maxStack)
  }
}
