package org.example.rlc.jvm.middleware

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

val javaLangObjectClassInfo = "java/lang/Object".toClassInfo()

val javaStringDescriptor = "Ljava/lang/String;".toUtf8Value()

val constructorMethodName = "<init>".toUtf8Value()
