package org.example.rlc.jvm.ir

val initializerNameAndType = NameAndTypeInfo(
  label = "<init>:V()", descriptor = "()V".toUtf8Value(), name = "<init>".toUtf8Value()
)

val objectConstructor = MethodRefInfo(
  label = "java/lang/Object.\"<init>\":()V",
  classInfo = "java/lang/Object".toClassInfo(),
  nameAndType = initializerNameAndType
)

val javaLangObjectClassInfo = "java/lang/Object".toClassInfo()

val javaStringDescriptor = "Ljava/lang/String;".toUtf8Value()

val constructorMethodName = "<init>".toUtf8Value()
