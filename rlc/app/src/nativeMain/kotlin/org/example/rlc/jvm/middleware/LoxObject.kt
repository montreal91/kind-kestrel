package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ByteConstantOperation
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
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
import org.example.rlc.jvm.ir.loxBoolean
import org.example.rlc.jvm.ir.loxDouble
import org.example.rlc.jvm.ir.loxMainClassName
import org.example.rlc.jvm.ir.loxNil
import org.example.rlc.jvm.ir.loxString
import org.example.rlc.jvm.ir.toUtf8Value


/***
 * The intermediate representation that corresponds the following Java code
 *
 * ```
 * class LoxObject {
 *   static final LoxClass loxNilClass = new LoxClass("LoxNil");
 *   static final LoxClass loxBoolClass = new LoxClass("LoxBoolean");
 *   static final LoxClass loxDoubleClass = new LoxClass("LoxDouble");
 *   static final LoxClass loxStringClass = new LoxClass("LoxString");
 *
 *   final LoxClass clazz;
 *
 *   LoxObject(LoxClass clazz) {
 *     this.clazz = clazz;
 *   }
 * }
 * ```
 */
internal fun loxObjectCf() = ClassFile(
  thisClassInfo = loxObjectClassInfo,
  superClassInfo = javaLangObjectClassInfo,
  accessFlagList = listOf(ClassAccessFlags.SUPER, ClassAccessFlags.ABSTRACT),
  attributeList = listOf(),
  fieldList = listOf(
    clazzFieldInfo(),
    loxDoubleClass(),
    loxNilClass(),
    loxBooleanClass(),
    loxStringClass()
  ),
  methodList = listOf(
    loxObjectConstructor(),
    loxObjectStaticInitializer(),
    abstractEqMethod(),
    abstractTruthyMethod(),
  ),
  interfaceList = listOf(),
)


private fun clazzFieldInfo() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "clazz".toUtf8Value(),
  fieldDescriptor = loxClassDescriptor,
)

private fun loxDoubleClass() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.STATIC, FieldAccessFlags.FINAL),
  fieldName = "LOX_DOUBLE_CLASS".toUtf8Value(),
  fieldDescriptor = loxClassDescriptor,
)

private fun loxNilClass() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.STATIC, FieldAccessFlags.FINAL),
  fieldName = "LOX_NIL_CLASS".toUtf8Value(),
  fieldDescriptor = loxClassDescriptor,
)

private fun loxBooleanClass() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.STATIC, FieldAccessFlags.FINAL),
  fieldName = "LOX_BOOLEAN_CLASS".toUtf8Value(),
  fieldDescriptor = loxClassDescriptor,
)

private fun loxStringClass() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.STATIC, FieldAccessFlags.FINAL),
  fieldName = "LOX_STRING_CLASS".toUtf8Value(),
  fieldDescriptor = loxClassDescriptor,
)

private fun loxObjectStaticInitializer(): MethodInfo {
  val loxClassConstructor = MethodRefInfo(
    label = "LoxClass.\"<init>\":(Ljava/lang/String;)V",
    classInfo = loxClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)V".toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 1
  )

  val loxDoubleClassField = FieldRefInfo(
    label = "LoxObject.LOX_DOUBLE_CLASS:LLoxClass;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_DOUBLE_CLASS:LLoxClass;",
      name = "LOX_DOUBLE_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val loxNilClassField = FieldRefInfo(
    label = "LoxObject.LOX_NIL_CLASS:LLoxClass;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_NIL_CLASS:LLoxClass;",
      name = "LOX_NIL_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val loxBoolClassField = FieldRefInfo(
    label = "LoxObject.LOX_BOOLEAN_CLASS:LLoxClass;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_BOOLEAN_CLASS:LLoxClass;",
      name = "LOX_BOOLEAN_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val loxStringClassField = FieldRefInfo(
    label = "LoxObject.LOX_STRING_CLASS:LLoxClass;",
    classInfo = loxObjectClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_STRING_CLASS:LLoxClass;",
      name = "LOX_STRING_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  // TODO: Create better code generator
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(loxDouble)),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxClassConstructor),
    ShortConstantOperation(Opcode.OP_PUTSTATIC, loxDoubleClassField),

    ShortConstantOperation(Opcode.OP_NEW, loxClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(loxNil)),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxClassConstructor),
    ShortConstantOperation(Opcode.OP_PUTSTATIC, loxNilClassField),

    ShortConstantOperation(Opcode.OP_NEW, loxClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(loxBoolean)),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxClassConstructor),
    ShortConstantOperation(Opcode.OP_PUTSTATIC, loxBoolClassField),

    ShortConstantOperation(Opcode.OP_NEW, loxClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    ByteConstantOperation(Opcode.OP_LDC, StringRefInfo(loxString)),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxClassConstructor),
    ShortConstantOperation(Opcode.OP_PUTSTATIC, loxStringClassField),

    SimpleOperation(Opcode.OP_RETURN),
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "<clinit>".toUtf8Value(),
    methodDescriptor = "()V".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.STATIC),
    attributeList = listOf(codeAttribute),
    // TODO: Make this stuff work
    isStatic = true,
    signature = MethodSignature(listOf(), ObjectVti(ClassInfo(loxMainClassName)))
  )
}

private fun loxObjectConstructor(): MethodInfo {
  val clazzField = FieldRefInfo(
    label = "LoxObject.clazz:LLoxClass;",
    classInfo = loxObjectClassInfo,
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
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "<init>".toUtf8Value(),
    methodDescriptor = "(LLoxClass;)V".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    // TODO: Make this stuff work
    isStatic = true,
    signature = MethodSignature(listOf(), ObjectVti(ClassInfo(loxMainClassName)))
  )
}

private fun abstractEqMethod(): MethodInfo = MethodInfo(
  methodName = "__eq__".toUtf8Value(),
  methodDescriptor = loxBinaryOpDescriptor,
  accessFlagList = listOf(MethodAccessFlags.ABSTRACT),
  attributeList = listOf(),
  // TODO: Make this stuff work
  isStatic = true,
  signature = MethodSignature(listOf(), ObjectVti(ClassInfo(loxMainClassName)))
)

// Later it will not be abstract
private fun abstractTruthyMethod(): MethodInfo = MethodInfo(
  methodName = "__truthy__".toUtf8Value(),
  methodDescriptor = loxUnaryOpDescriptor,
  accessFlagList = listOf(MethodAccessFlags.ABSTRACT),
  attributeList = listOf(),
  // TODO: Make this stuff work
  isStatic = true,
  signature = MethodSignature(listOf(), ObjectVti(ClassInfo(loxMainClassName)))
)

internal fun alwaysTruthy(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ICONST_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "__truthy__".toUtf8Value(),
    methodDescriptor = loxUnaryOpDescriptor,
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    // TODO: Make this stuff work
    isStatic = true,
    signature = MethodSignature(listOf(), ObjectVti(ClassInfo(loxMainClassName)))
  )
}
