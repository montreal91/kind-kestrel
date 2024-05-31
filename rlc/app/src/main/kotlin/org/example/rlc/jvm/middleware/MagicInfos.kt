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

internal val loxDoubleConstructorInfo = MethodRefInfo(
  label = "LoxDouble.\"<init>\":(D)V",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(D)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(D)V".toUtf8Value(),
  )
)

internal val valueFieldRefInfo = FieldRefInfo(
  label = "LoxDouble.value:D",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "value:D",
    name = "value".toUtf8Value(),
    descriptor = "D".toUtf8Value(),
  )
)

internal val initializerNameAndType = NameAndTypeInfo(
  label = "<init>:V()", descriptor = "()V".toUtf8Value(), name = "<init>".toUtf8Value()
)

internal val objectConstructor = MethodRefInfo(
  label = "java/lang/Object.\"<init>\":()V",
  classInfo = "java/lang/Object".toClassInfo(),
  nameAndType = initializerNameAndType
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
  )
)

internal val arithmeticOperations = mapOf(
  Pair(
    Token.Type.PLUS, NameAndTypeInfo(
      label = "__add__:(LLoxObject;LLoxObject;)LLoxObject;",
      name = "__add__".toUtf8Value(),
      descriptor = "(LLoxObject;LLoxObject;)LLoxObject;".toUtf8Value()
    )
  ),
  Pair(
    Token.Type.MINUS, NameAndTypeInfo(
      label = "__sub__:(LLoxObject;LLoxObject;)LLoxObject;",
      name = "__sub__".toUtf8Value(),
      descriptor = "(LLoxObject;LLoxObject;)LLoxObject;".toUtf8Value()
    )
  ),
  Pair(
    Token.Type.STAR, NameAndTypeInfo(
      label = "__mul__:(LLoxObject;LLoxObject;)LLoxObject;",
      name = "__mul__".toUtf8Value(),
      descriptor = "(LLoxObject;LLoxObject;)LLoxObject;".toUtf8Value()
    )
  ),
  Pair(
    Token.Type.SLASH, NameAndTypeInfo(
      label = "__div__:(LLoxObject;LLoxObject;)LLoxObject;",
      name = "__div__".toUtf8Value(),
      descriptor = "(LLoxObject;LLoxObject;)LLoxObject;".toUtf8Value()
    )
  ),
)
