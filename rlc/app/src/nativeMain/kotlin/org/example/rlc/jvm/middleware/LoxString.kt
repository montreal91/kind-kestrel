package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toUtf8Value

internal fun loxString() = ClassFile(
  thisClassInfo = loxStringClassInfo,
  superClassInfo = loxObjectClassInfo,
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  fieldList = listOf(valueFieldInfo()),
  methodList = listOf(
    loxStringConstructor(),
    stringConcatenation(),
    toString(),
    eq(),
    truthy(),
  ),
  interfaceList = listOf(),
  attributeList = listOf(),
)

private fun valueFieldInfo() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "value".toUtf8Value(),
  fieldDescriptor = "Ljava/lang/String;".toUtf8Value(),
)

private fun loxStringConstructor(): MethodInfo {
  val loxStringClass = FieldRefInfo(
    label = "LoxObject.LOX_STRING_CLASS:LLoxClass;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_STRING_CLASS:LLoxClass;",
      name = "LOX_STRING_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETSTATIC, loxStringClass),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_PUTFIELD, stringValueFieldRefInfo),
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
    methodDescriptor = "(Ljava/lang/String;)V".toUtf8Value(),
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
  )
}

private fun toString(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, stringValueFieldRefInfo),
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
    attributeList = listOf(codeAttribute)
  )
}

private fun eq(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, stringValueFieldRefInfo),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxStringClassInfo),
    ShortConstantOperation(Opcode.OP_GETFIELD, stringValueFieldRefInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, stringEquals()),
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
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
  )
}

private fun truthy() = alwaysTruthy()

private fun stringConcatenation(): MethodInfo {
  val sb = "java/lang/StringBuilder".toClassInfo()
  val sbConstructorMethodRef = MethodRefInfo(
    label = "java/lang/StringBuilder.\"<init>\":(Ljava/lang/String;)V",
    classInfo = sb,
    argsSize = 2,
    returnSize = 1,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)V".toUtf8Value(),
    )
  )

  val sbAppendMethodRef = MethodRefInfo(
    label = "java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
    classInfo = sb,
    argsSize = 2,
    returnSize = 1,
    nameAndType = NameAndTypeInfo(
      label = "append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
      name = "append".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)Ljava/lang/StringBuilder;".toUtf8Value(),
    )
  )

  val sbToStringMethodRef = MethodRefInfo(
    label = "java/lang/StringBuilder.toString:()Ljava/lang/String;",
    classInfo = sb,
    argsSize = 1,
    returnSize = 1,
    nameAndType = NameAndTypeInfo(
      label = "toString:()Ljava/lang/String;",
      name = "toString".toUtf8Value(),
      descriptor = "()Ljava/lang/String;".toUtf8Value()
    )
  )
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, sb),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, stringValueFieldRefInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, sbConstructorMethodRef),
    SimpleOperation(Opcode.OP_ASTORE_2),
    SimpleOperation(Opcode.OP_ALOAD_2),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, stringValueFieldRefInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, sbAppendMethodRef),
    SimpleOperation(Opcode.OP_POP),
    ShortConstantOperation(Opcode.OP_NEW, loxStringClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_2),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, sbToStringMethodRef),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxStringConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "__add__".toUtf8Value(),
    methodDescriptor = "(LLoxString;)LLoxString;".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
  )
}
