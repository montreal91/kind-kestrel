package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value


internal val objectInitializer = "<init>".toUtf8Value()

internal fun runtimeErrorConstructorRef(): MethodRefInfo {
  return MethodRefInfo(
    label = "LoxRuntimeError.\"<init>\":(Ljava/lang/String;)V",
    classInfo = loxRuntimeErrorClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = objectInitializer,
      descriptor = "(Ljava/lang/String;)V".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1
  )
}

// Add method is a bit different because
// Lox supports concatenation of strings with + operator
internal fun addMethod(): MethodInfo {
  // To properly implement Lox runtime errors, this (and other) magic methods should be inlined.
  val doubleAddMagicMethod = MethodRefInfo(
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

  val stringConcatMagicMethod = MethodRefInfo(
    label = "LoxString.__add__:(LLoxString;)LLoxString;",
    classInfo = loxStringClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "__add__:(LLoxString;)LLoxString;",
      name = "__add__".toUtf8Value(),
      descriptor = "(LLoxString;)LLoxString;".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 1
  )

  val errorMessage = "Both operands should be numbers or strings."

  // This code will change after a while
  val code = mutableListOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxDoubleClassInfo),
    // Hmm, maybe I need something like labels for these purposes
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
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, doubleAddMagicMethod),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxStringClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 32),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxStringClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 32),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxStringClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_2),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxStringClassInfo),
    SimpleOperation(Opcode.OP_ASTORE_3),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_3),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, stringConcatMagicMethod),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  code.addAll(generateRuntimeError(errorMessage))

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code.toList(),
  )

  return makeBinaryMethodInfo(methodName = "__add__", codeAttribute = codeAttribute)
}

internal fun equalsMethod(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, objectEqualsMri),
    ControlFlowOperation(Opcode.OP_IFNE, jumpTo = 11),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxNilClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 19),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectEqMri),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return makeBinaryMethodInfo(methodName = "__eq__", codeAttribute = codeAttribute)
}

internal fun numberMagicMethod(methodName: String, returnType: String): MethodInfo {
  val methodRefInfo = MethodRefInfo(
    label = "LoxDouble.${methodName}:(LLoxDouble;)L$returnType;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "${methodName}:(LLoxDouble;)L$returnType;",
      name = methodName.toUtf8Value(),
      descriptor = "(LLoxDouble;)L$returnType;".toUtf8Value()
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
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(value = "Operands must be numbers.")),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
    SimpleOperation(Opcode.OP_ATHROW),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return makeBinaryMethodInfo(methodName, codeAttribute)
}

internal fun notEqualsMethod(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, getClassMri),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, objectEqualsMri),
    ControlFlowOperation(Opcode.OP_IFNE, jumpTo = 11),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxNilClassInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 19),
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxObjectEqMri),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxBooleanNegMri()),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code)

  return makeBinaryMethodInfo(methodName = "__neq__", codeAttribute = codeAttribute)
}

private fun makeBinaryMethodInfo(methodName: String, codeAttribute: CodeAttribute) = MethodInfo(
  methodName = methodName,
  accessFlagList = listOf(
    MethodAccessFlags.STATIC,
    MethodAccessFlags.FINAL,
    MethodAccessFlags.PRIVATE,
  ),
  attributeList = listOf(codeAttribute),
  isStatic = true,
  signature = MethodSignature(
    listOf(ObjectVti(loxObjectClassInfo), ObjectVti(loxObjectClassInfo)),
    ObjectVti(loxObjectClassInfo)
  )
)

private fun makeUnaryMethodInfo(methodName: String, codeAttribute: CodeAttribute) = MethodInfo(
  methodName = methodName,
  accessFlagList = listOf(
    MethodAccessFlags.STATIC,
    MethodAccessFlags.FINAL,
    MethodAccessFlags.PRIVATE,
  ),
  attributeList = listOf(codeAttribute),
  isStatic = true,
  signature = MethodSignature(listOf(ObjectVti(loxObjectClassInfo)), ObjectVti(loxObjectClassInfo))
)

internal fun unaryMinusMagicMethod(methodName: String): MethodInfo {
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

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code + generateRuntimeError("Operand should be a number."),
  )

  return MethodInfo(
    methodName = methodName,
    accessFlagList = listOf(
      MethodAccessFlags.STATIC,
      MethodAccessFlags.FINAL,
      MethodAccessFlags.PRIVATE
    ),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(ObjectVti(loxObjectClassInfo)), ObjectVti(loxObjectClassInfo))
  )
}

internal fun notOperatorMagicMethod(): MethodInfo {
  val methodName = "__truthy__"
  val objectTruthy = MethodRefInfo(
    label = "LoxObject.$methodName:()LLoxObject;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "$methodName:()LLoxObject;",
      name = methodName.toUtf8Value(),
      descriptor = "()LLoxObject;".toUtf8Value()
    ),
    argsSize = 1,
    returnSize = 1
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, objectTruthy),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, loxBooleanNegMri()),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
  )

  return makeUnaryMethodInfo(methodName = "__not__", codeAttribute = codeAttribute)
}

internal fun generateRuntimeError(message: String) = listOf(
  ShortConstantOperation(Opcode.OP_NEW, loxRuntimeErrorClassInfo),
  SimpleOperation(Opcode.OP_DUP),
  ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(message)),
  ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeErrorConstructorRef()),
  SimpleOperation(Opcode.OP_ATHROW),
)
