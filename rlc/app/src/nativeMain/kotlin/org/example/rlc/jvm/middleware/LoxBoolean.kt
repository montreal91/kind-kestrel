package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toStringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value

internal fun loxBoolean() = ClassFile(
  thisClassInfo = loxBooleanClassInfo,
  superClassInfo = loxObjectClassInfo,
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  attributeList = listOf(),
  fieldList = listOf(valueField()),
  methodList = listOf(loxBooleanConstructor(), toString(), eq()),
  interfaceList = listOf(),
)

internal val booleanValueFieldRefInfo = FieldRefInfo(
  label = "LoxBoolean.value:Z",
  classInfo = loxBooleanClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "value:Z",
    name = "value".toUtf8Value(),
    descriptor = "Z".toUtf8Value(),
  )
)

private fun valueField() = FieldInfo(
  fieldName = "value".toUtf8Value(),
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldDescriptor = "Z".toUtf8Value(),
)

private fun loxBooleanConstructor(): MethodInfo {
  val loxBooleanClass = FieldRefInfo(
    label = "LoxObject.LOX_BOOLEAN_CLASS:LLoxClass;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_BOOLEAN_CLASS:LLoxClass;",
      name = "LOX_BOOLEAN_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETSTATIC, loxBooleanClass),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ILOAD_1),
    ShortConstantOperation(Opcode.OP_PUTFIELD, booleanValueFieldRefInfo),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "<init>".toUtf8Value(),
    methodDescriptor = "(Z)V".toUtf8Value(),
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
  )
}

private fun eq(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, booleanValueFieldRefInfo),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxBooleanClassInfo),
    ShortConstantOperation(Opcode.OP_GETFIELD, booleanValueFieldRefInfo),
    SimpleOperation(Opcode.OP_IXOR),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_IXOR),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_IAND),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
  )

  return MethodInfo(
    methodName = "__eq__".toUtf8Value(),
    methodDescriptor = loxBinaryOpDescriptor,
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
  )
}

private fun toString(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, booleanValueFieldRefInfo),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 5),
    ByteConstantOperation(Opcode.OP_LDC, "true".toStringRefInfo()),
    SimpleOperation(Opcode.OP_ARETURN),
    ByteConstantOperation(Opcode.OP_LDC, "false".toStringRefInfo()),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "toString".toUtf8Value(),
    methodDescriptor = toStringDescriptor,
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(codeAttribute),
  )
}
