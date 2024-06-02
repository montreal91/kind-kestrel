package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toStringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value

internal fun runtimeErrorConstructorRef(): MethodRefInfo {
  return MethodRefInfo(
    label = "LoxRuntimeError.\"<init>\":(Ljava/lang/String;)V",
    classInfo = "LoxRuntimeError".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)V".toUtf8Value()
    )
  )
}

// Add method is a bit different because
// Lox supports concatenation of strings with + operator
internal fun addMethod(): MethodInfo {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.__add__:(LLoxDouble;)LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__add__:(LLoxDouble;)LLoxDouble;",
      name = "__add__".toUtf8Value(),
      descriptor = "(LLoxDouble;)LLoxDouble;".toUtf8Value()
    )
  )


  // This code will change after a while
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
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeError),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, "Both operands should be double.".toStringRefInfo()),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  val codeAttribute = CodeAttribute(
    maxStack = 3,
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
  )

  return MethodInfo(
    methodName = "__add__".toUtf8Value(),
    methodDescriptor = "(LLoxObject;LLoxObject;)LLoxObject;".toUtf8Value(),
    accessFlagList = listOf(
      MethodAccessFlags.STATIC,
      MethodAccessFlags.FINAL,
      MethodAccessFlags.PRIVATE,
    ),
    attributes = listOf(codeAttribute)
  )
}

internal fun numberMagicMethod(methodName: String): MethodInfo {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.${methodName}:(LLoxDouble;)LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "${methodName}:(LLoxDouble;)LLoxDouble;",
      name = methodName.toUtf8Value(),
      descriptor = "(LLoxDouble;)LLoxDouble;".toUtf8Value()
    )
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
    ShortConstantOperation(Opcode.OP_NEW, loxRuntimeError),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, "Both operands should be double.".toStringRefInfo()),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  val codeAttribute = CodeAttribute(
    maxStack = 3,
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
  )

  return MethodInfo(
    methodName = methodName.toUtf8Value(),
    methodDescriptor = "(LLoxObject;LLoxObject;)LLoxObject;".toUtf8Value(),
    accessFlagList = listOf(
      MethodAccessFlags.STATIC,
      MethodAccessFlags.FINAL,
      MethodAccessFlags.PRIVATE,
    ),
    attributes = listOf(codeAttribute)
  )
}

internal fun unaryMagicMethod(methodName: String): MethodInfo {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.${methodName}:()LLoxDouble;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "${methodName}:()LLoxDouble;",
      name = methodName.toUtf8Value(),
      descriptor = "()LLoxDouble;".toUtf8Value()
    )
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

  val codeAttribute = CodeAttribute(
    maxStack = 3,
    argsSize = 1,
    code = code + generateRuntimeError("Operand should be a number."),
    exceptionTable = 0,
    attributes = listOf(),
  )

  return MethodInfo(
    methodName = methodName.toUtf8Value(),
    methodDescriptor = "(LLoxObject;)LLoxObject;".toUtf8Value(),
    accessFlagList = listOf(
      MethodAccessFlags.STATIC,
      MethodAccessFlags.FINAL,
      MethodAccessFlags.PRIVATE
    ),
    attributes = listOf(codeAttribute)
  )
}

private fun generateRuntimeError(message: String) = listOf(
  ShortConstantOperation(Opcode.OP_NEW, loxRuntimeError),
  SimpleOperation(Opcode.OP_DUP),
  ByteConstantOperation(Opcode.OP_LDC, message.toStringRefInfo()),
  ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
  SimpleOperation(Opcode.OP_ATHROW),
)
