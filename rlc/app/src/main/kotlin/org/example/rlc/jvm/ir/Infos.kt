package org.example.rlc.jvm.ir

val loxObjectClassInfo = "LoxObject".toClassInfo()
val loxDoubleClassInfo = "LoxDouble".toClassInfo()

val valueFieldRefInfo = FieldRefInfo(
  label = "LoxDouble.value:D",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "value:D",
    name = "value".toUtf8Value(),
    descriptor = "D".toUtf8Value(),
  )
)

val loxDoubleConstructorInfo = MethodRefInfo(
  label = "LoxDouble.\"<init>\":(D)V",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(D)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(D)V".toUtf8Value(),
  )
)
