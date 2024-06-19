package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.Token
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toUtf8Value


internal val loxRuntimeError = "LoxRuntimeError".toClassInfo()
internal val loxDoubleClassInfo = "LoxDouble".toClassInfo()
internal val loxObjectClassInfo = "LoxObject".toClassInfo()
internal val loxNilClassInfo = "LoxNil".toClassInfo()
internal val loxBooleanClassInfo = "LoxBoolean".toClassInfo()
internal val loxClassInfo = "LoxClass".toClassInfo()
internal val loxStringClassInfo = "LoxString".toClassInfo()

internal val loxDoubleConstructorInfo = MethodRefInfo(
  label = "LoxDouble.\"<init>\":(D)V",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(D)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(D)V".toUtf8Value(),
  ),
  argsSize = 3,
  returnSize = 1
)

internal val loxBooleanConstructorInfo = MethodRefInfo(
  label = "LoxBoolean.\"<init>\":(D)V",
  classInfo = loxBooleanClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(Z)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(Z)V".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 1
)

internal val loxNilConstructorInfo = MethodRefInfo(
  label = "LoxNil.\"<init>\":()V",
  classInfo = loxNilClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":()V",
    name = "<init>".toUtf8Value(),
    descriptor = "()V".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 1
)

internal val loxStringConstructorInfo = MethodRefInfo(
  label = "LoxString.\"<init>\":(Ljava/lang/String;)V",
  classInfo = loxStringClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(Ljava/lang/String;)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(Ljava/lang/String;)V".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 1
)

internal val doubleValueFieldRefInfo = FieldRefInfo(
  label = "LoxDouble.value:D",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "value:D",
    name = "value".toUtf8Value(),
    descriptor = "D".toUtf8Value(),
  )
)

internal val stringValueFieldRefInfo = FieldRefInfo(
  label = "LoxString.value:Ljava/lang/String;",
  classInfo = loxStringClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "value:Ljava/lang/String;",
    name = "value".toUtf8Value(),
    descriptor = "Ljava/lang/String;".toUtf8Value(),
  )
)

internal val initializerNameAndType = NameAndTypeInfo(
  label = "<init>:V()", descriptor = "()V".toUtf8Value(), name = "<init>".toUtf8Value()
)

internal val objectConstructor = MethodRefInfo(
  label = "java/lang/Object.\"<init>\":()V",
  classInfo = "java/lang/Object".toClassInfo(),
  nameAndType = initializerNameAndType,
  argsSize = 1,
  returnSize = 1
)

internal val loxObjectConstructor = MethodRefInfo(
  label = "LoxObject.\"<init>\":(LLoxClass;)V",
  classInfo = loxObjectClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(LLoxClass;)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(LLoxClass;)V".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 1
)

internal val javaLangObjectClassInfo = "java/lang/Object".toClassInfo()

internal val javaStringDescriptor = "Ljava/lang/String;".toUtf8Value()

internal val constructorMethodName = "<init>".toUtf8Value()

internal val systemOutField = FieldRefInfo(
  label = "java/lang/System.out:Ljava/io/PrintStream;",
  classInfo = "java/lang/System".toClassInfo(),
  nameAndType = NameAndTypeInfo(
    label = "System.err:PrintStream",
    name = "out".toUtf8Value(),
    descriptor = "Ljava/io/PrintStream;".toUtf8Value(),
  )
)

internal val printMethodRef = MethodRefInfo(
  label = "java/io/PrintStream.println:(Ljava/lang/Object;)V",
  classInfo = "java/io/PrintStream".toClassInfo(),
  nameAndType = NameAndTypeInfo(
    label = "println:(Ljava/lang/Object;)V",
    name = "println".toUtf8Value(),
    descriptor = "(Ljava/lang/Object;)V".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 0
)

private const val binaryOpDescriptor = "(LLoxObject;LLoxObject;)LLoxObject;"
private const val unaryOpDescriptor = "(LLoxObject;)LLoxObject;"

internal val binaryOperations = mapOf(
  Pair(Token.Type.PLUS, genBinaryNameAndType(name = "__add__")),
  Pair(Token.Type.MINUS, genBinaryNameAndType(name="__sub__")),
  Pair(Token.Type.STAR, genBinaryNameAndType(name = "__mul__")),
  Pair(Token.Type.SLASH, genBinaryNameAndType(name = "__div__")),
  Pair(Token.Type.EQUAL_EQUAL, genBinaryNameAndType(name = "__eq__")),
  Pair(Token.Type.BANG_EQUAL, genBinaryNameAndType(name = "__ne__")),
)

private fun genBinaryNameAndType(name: String) = NameAndTypeInfo(
  label = "$name:$binaryOpDescriptor",
  name = name.toUtf8Value(),
  descriptor = binaryOpDescriptor.toUtf8Value(),
)

internal val unaryOperations = mapOf(
  Pair(
    Token.Type.MINUS, NameAndTypeInfo(
      label = "__neg__:${unaryOpDescriptor}",
      name = "__neg__".toUtf8Value(),
      descriptor = unaryOpDescriptor.toUtf8Value()
    )
  )
)
