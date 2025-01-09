package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.Token
import org.example.rlc.jvm.ir.BooleanVti
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.javaLangObject
import org.example.rlc.jvm.ir.loxBoolean
import org.example.rlc.jvm.ir.loxDouble
import org.example.rlc.jvm.ir.loxMainClassName
import org.example.rlc.jvm.ir.loxNil
import org.example.rlc.jvm.ir.loxObject
import org.example.rlc.jvm.ir.loxString
import org.example.rlc.jvm.ir.toUtf8Value


internal val javaLangObjectClassInfo = ClassInfo(className = javaLangObject)
internal val javaLangStringClassInfo = ClassInfo(className = "java/lang/String")
internal val loxRuntimeErrorClassInfo = ClassInfo(className = "LoxRuntimeError")
internal val loxDoubleClassInfo = ClassInfo(className = loxDouble)
internal val loxObjectClassInfo = ClassInfo(className = loxObject)
internal val loxNilClassInfo = ClassInfo(className = loxNil)
internal val loxBooleanClassInfo = ClassInfo(className = loxBoolean)
internal val loxClassInfo = ClassInfo(className = "LoxClass")
internal val loxStringClassInfo = ClassInfo(className = loxString)
internal val loxMainClassInfo = ClassInfo(className = loxMainClassName)

internal val loxDoubleConstructorInfo = MethodRefInfo(
  label = "LoxDouble.\"<init>\":(D)V",
  classInfo = loxDoubleClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "\"<init>\":(D)V",
    name = "<init>".toUtf8Value(),
    descriptor = "(D)V".toUtf8Value(),
  ),
  argsSize = 3,
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
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
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
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
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
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
  returnSize = 1,
  returnTypeInfo = EmptyVti()
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
  classInfo = javaLangObjectClassInfo,
  nameAndType = initializerNameAndType,
  argsSize = 1,
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
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
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
)

internal val javaStringDescriptor = "Ljava/lang/String;".toUtf8Value()

internal const val constructorMethodName = "<init>"
internal const val staticInitializerMethodName = "<clinit>"

internal val systemOutField = FieldRefInfo(
  label = "java/lang/System.out:Ljava/io/PrintStream;",
  classInfo = ClassInfo(className = "java/lang/System"),
  nameAndType = NameAndTypeInfo(
    label = "System.err:PrintStream",
    name = "out".toUtf8Value(),
    descriptor = "Ljava/io/PrintStream;".toUtf8Value(),
  )
)

internal val printMethodRef = MethodRefInfo(
  label = "java/io/PrintStream.println:(Ljava/lang/Object;)V",
  classInfo = ClassInfo(className = "java/io/PrintStream"),
  nameAndType = NameAndTypeInfo(
    label = "println:(Ljava/lang/Object;)V",
    name = "println".toUtf8Value(),
    descriptor = "(Ljava/lang/Object;)V".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 0,
  returnTypeInfo = EmptyVti(),
)

private const val binaryOpDescriptor = "(LLoxObject;LLoxObject;)LLoxObject;"
private const val unaryOpDescriptor = "(LLoxObject;)LLoxObject;"

internal val binaryOperations = mapOf(
  Pair(Token.Type.PLUS, genBinaryNameAndType(name = "__add__")),
  Pair(Token.Type.MINUS, genBinaryNameAndType(name="__sub__")),
  Pair(Token.Type.STAR, genBinaryNameAndType(name = "__mul__")),
  Pair(Token.Type.SLASH, genBinaryNameAndType(name = "__div__")),
  Pair(Token.Type.EQUAL_EQUAL, genBinaryNameAndType(name = "__eq__")),
  Pair(Token.Type.BANG_EQUAL, genBinaryNameAndType(name = "__neq__")),
  Pair(Token.Type.GREATER, genBinaryNameAndType(name = "__gt__")),
  Pair(Token.Type.GREATER_EQUAL, genBinaryNameAndType(name = "__ge__")),
  Pair(Token.Type.LESS, genBinaryNameAndType(name = "__lt__")),
  Pair(Token.Type.LESS_EQUAL, genBinaryNameAndType(name = "__le__")),
)

internal fun stringEquals(): MethodRefInfo {
  val nameInfo = "equals".toUtf8Value()
  val typeInfo = "(Ljava/lang/Object;)Z".toUtf8Value()
  return MethodRefInfo(
    label = "java/lang/String.equals:(Ljava/lang/Object;)Z",
    classInfo = ClassInfo(className = "java/lang/String"),
    nameAndType = NameAndTypeInfo(
      label = "equals:(Ljava/lang/Object;)Z",
      name = nameInfo,
      descriptor = typeInfo,
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = BooleanVti(),
  )
}

internal fun loxBooleanNegMri(): MethodRefInfo {
  val nameInfo = "__neg__".toUtf8Value()
  val typeInfo = "()LLoxObject;".toUtf8Value()

  return MethodRefInfo(
    label = "LoxBoolean.$nameInfo:$typeInfo",
    classInfo = loxBooleanClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "$nameInfo:$typeInfo",
      name = nameInfo,
      descriptor = typeInfo
    ),
    argsSize = 1,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false)
  )
}

private fun genBinaryNameAndType(name: String) = NameAndTypeInfo(
  label = "$name:$binaryOpDescriptor",
  name = name.toUtf8Value(),
  descriptor = binaryOpDescriptor.toUtf8Value(),
)

internal val unaryOperations = mapOf(
  Pair(Token.Type.MINUS, genUnaryNameAndType(name = "__neg__")),
  Pair(Token.Type.BANG, genUnaryNameAndType(name = "__not__"))
)

private fun genUnaryNameAndType(name: String) = NameAndTypeInfo(
  label = "$name:$unaryOpDescriptor",
  name = name.toUtf8Value(),
  descriptor = unaryOpDescriptor.toUtf8Value()
)

internal val getClassMri = MethodRefInfo(
  label = "java/lang/Object.getClass:()Ljava/lang/Class;",
  classInfo = javaLangObjectClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "getClass:()Ljava/lang/Class;",
    name = "getClass".toUtf8Value(),
    descriptor = "()Ljava/lang/Class;".toUtf8Value()
  ),
  argsSize = 1,
  returnSize = 1,
  returnTypeInfo = ObjectVti(javaLangObjectClassInfo, isArray = false)
)

internal val objectEqualsMri = MethodRefInfo(
  label = "java/lang/Object.equals:(Ljava/lang/Object;)Z",
  classInfo = javaLangObjectClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "equals:(Ljava/lang/Object;)Z",
    name = "equals".toUtf8Value(),
    descriptor = "(Ljava/lang/Object;)Z".toUtf8Value(),
  ),
  argsSize = 2,
  returnSize = 1,
  returnTypeInfo = BooleanVti(),
)

internal val loxObjectEqMri = MethodRefInfo(
  label = "LoxObject.__eq__:(LLoxObject;)LLoxObject;",
  classInfo = loxObjectClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "__eq__:(LLoxObject;)LLoxObject;",
    name = "__eq__".toUtf8Value(),
    descriptor = loxBinaryOpDescriptor,
  ),
  argsSize = 2,
  returnSize = 1,
  returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false),
)

internal val loxObjectTruthyMri = MethodRefInfo(
  label = "LoxObject.__truthy__:(LLoxObject;)LLoxObject;",
  classInfo = loxObjectClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "__truthy__:(LLoxObject;)LLoxObject;",
    name = "__truthy__".toUtf8Value(),
    descriptor = loxUnaryOpDescriptor,
  ),
  argsSize = 1,
  returnSize = 1,
  returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false)
)
