package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value

internal object LoxNil {
  internal fun loxNilCf() = ClassFile(
    thisClassInfo = loxNilClassInfo,
    superClassInfo = loxObjectClassInfo,
    accessFlagList = listOf(ClassAccessFlags.SUPER),
    attributeList = listOf(),
    fieldList = listOf(),
    methodList = listOf(loxNilConstructor(), nilToString(), truthy()),
    interfaceList = listOf(),
  )

  internal fun generateNil() = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxNilClassInfo, ObjectVti(loxObjectClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxNilConstructorInfo),
  )
}

private fun loxNilConstructor(): MethodInfo {
  val loxNilClass = FieldRefInfo(
    label = "LoxObject.LOX_NIL_CLASS:LLoxClass;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_NIL_CLASS:LLoxClass;",
      name = "LOX_NIL_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETSTATIC, loxNilClass, ObjectVti(loxNilClassInfo)),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 1
  )

  return MethodInfo(
    methodName = constructorMethodName,
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
    isStatic = false,
    signature = MethodSignature(listOf(), EmptyVti())
  )
}

private fun truthy(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 0
  )

  return MethodInfo(
    methodName = "__truthy__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = loxUnaryOpSignature
  )
}

private val nilStr = StringRefInfo(value = "nil")

private fun nilToString(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ByteConstantOperation(Opcode.OP_LDC, nilStr, JavaString.VERIFICATION_TYPE),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 1
  )

  return MethodInfo(
    methodName = "toString",
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(), JavaString.VERIFICATION_TYPE),
  )
}
