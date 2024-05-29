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


/***
 * The intermediate representation that corresponds the following Java code
 *
 * ```
 * class LoxDouble extends LoxObject {
 *   final double value;
 *   LoxDouble(double value) {
 *     super(LoxObject.loxDoubleClass);
 *     this.value = value;
 *   }
 *
 *   LoxDouble __add__(LoxDouble other) {
 *     return new LoxDouble(value + other.value);
 *   }
 *
 *   @Override
 *   public String toString() {
 *     return Double.toString(this.value);
 *   }
 * }
 * ```
 */
internal fun loxDouble() = ClassFile(
  thisClassInfo = "LoxDouble".toClassInfo(),
  superClassInfo = "LoxObject".toClassInfo(),
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  attributeList = listOf(),
  fieldList = listOf(valueFieldInfo()),
  methodList = listOf(loxDoubleConstructor(), addMethodInfo(), toString()),
  interfaceList = listOf(),
)

private fun valueFieldInfo() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "value".toUtf8Value(),
  fieldDescriptor = "D".toUtf8Value(),
)

private fun loxDoubleConstructor(): MethodInfo {
  val loxDoubleClass = FieldRefInfo(
    label = "LoxObject.loxDoubleClass:LLoxClass;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "loxDoubleClass:LLoxClass;",
      name = "loxDoubleClass".toUtf8Value(),
      descriptor = "LLoxClass;".toUtf8Value(),
    )
  )

  val loxObjectConstructor = MethodRefInfo(
    label = "LoxObject.\"<init>\":(LLoxClass;)V",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(LLoxClass;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(LLoxClass;)V".toUtf8Value(),
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETSTATIC, loxDoubleClass),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_DLOAD_0),
    ShortConstantOperation(Opcode.OP_PUTFIELD, valueFieldRefInfo),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(
    maxStack = 3,
    maxLocals = 3,
    attributes = listOf(),
    code = code,
    exceptionTable = 0
  )

  return MethodInfo(
    methodName = "<init>".toUtf8Value(),
    methodDescriptor = "(D)V".toUtf8Value(),
    accessFlagList = listOf(),
    attributes = listOf(codeAttribute),
  )
}

private fun addMethodInfo(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, valueFieldRefInfo),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, valueFieldRefInfo),
    SimpleOperation(Opcode.OP_DADD),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    maxStack = 6,
    maxLocals = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "__add__".toUtf8Value(),
    methodDescriptor = "(LLoxDouble;)LLoxDouble;".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributes = listOf(codeAttribute),
  )
}

private fun toString(): MethodInfo {
  val doubleToStringMethodRef = MethodRefInfo(
    label = "java/lang/Double.toString:(D)Ljava/lang/String;",
    classInfo = "java/lang/Double".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "toString:(D)Ljava/lang/String;",
      name = "toString".toUtf8Value(),
      descriptor = "(D)Ljava/lang/String;".toUtf8Value(),
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, valueFieldRefInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_STATIC, doubleToStringMethodRef),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    maxStack = 6,
    maxLocals = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "toString".toUtf8Value(),
    methodDescriptor = "()Ljava/lang/String;".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributes = listOf(codeAttribute)
  )
}
