package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
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
import org.example.rlc.jvm.ir.toStringRefInfo
import org.example.rlc.jvm.ir.toUtf8Value


/***
 * The intermediate representation that corresponds the following Java code
 *
 * ```
 * class LoxObject {
 *   static LoxClass loxDoubleClass = new LoxClass("LoxDouble");
 *   final LoxClass clazz;
 *
 *   LoxObject(LoxClass clazz) {
 *     this.clazz = clazz;
 *   }
 * }
 * ```
 */
internal fun loxObject() = ClassFile(
  thisClassInfo = "LoxObject".toClassInfo(),
  superClassInfo = "java/lang/Object".toClassInfo(),
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  attributeList = listOf(),
  fieldList = listOf(clazzFieldInfo(), loxDoubleClass()),
  methodList = listOf(loxObjectConstructor(), loxObjectStaticInitializer()),
  interfaceList = listOf(),
)


private fun clazzFieldInfo() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "clazz".toUtf8Value(),
  fieldDescriptor = "LLoxClass;".toUtf8Value(),
)

private fun loxDoubleClass() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.STATIC, FieldAccessFlags.FINAL),
  fieldName = "LOX_DOUBLE_CLASS".toUtf8Value(),
  fieldDescriptor = "LLoxClass;".toUtf8Value(),
)

private fun loxObjectStaticInitializer(): MethodInfo {
  val loxClassConstructor = MethodRefInfo(
    label = "LoxClass.\"<init>\":(Ljava/lang/String;)V",
    classInfo = "LoxClass".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)V".toUtf8Value(),
    )
  )

  val loxClassField = FieldRefInfo(
    label = " LoxObject.LOX_DOUBLE_CLASS:LLoxClass;",
    classInfo = "LoxObject".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "LOX_DOUBLE_CLASS:LLoxClass;",
      name = "LOX_DOUBLE_CLASS".toUtf8Value(),
      descriptor = "LLoxClass;".toUtf8Value(),
    )
  )

  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, "LoxClass".toClassInfo()),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, "LoxDouble".toStringRefInfo()),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxClassConstructor),
    ShortConstantOperation(Opcode.OP_PUTSTATIC, loxClassField),
    SimpleOperation(Opcode.OP_RETURN),
  )

  val codeAttribute = CodeAttribute(
    code = code,
    maxStack = 3,
    maxLocals = 0,
    attributes = listOf(),
    exceptionTable = 0
  )

  return MethodInfo(
    methodName = "<clinit>".toUtf8Value(),
    methodDescriptor = "()V".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributes = listOf(codeAttribute)
  )
}

fun loxObjectConstructor(): MethodInfo {
  val clazzField = FieldRefInfo(
    label = "LoxObject.clazz:LLoxClass;",
    classInfo = "LoxObject".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "clazz:LLoxClass;",
      name = "clazz".toUtf8Value(),
      descriptor = "LLoxClass;".toUtf8Value(),
    )
  )
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, objectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_PUTFIELD, clazzField),
    SimpleOperation(Opcode.OP_RETURN),
  )

  val codeAttribute = CodeAttribute(
    code = code,
    maxStack = 2,
    maxLocals = 2,
    attributes = listOf(),
    exceptionTable = 0
  )

  return MethodInfo(
    methodName = "<init>".toUtf8Value(),
    methodDescriptor = "(LLoxClass;)V".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributes = listOf(codeAttribute),
  )
}
