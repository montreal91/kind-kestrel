package org.example.rlc.jvm.ir

import org.example.rlc.jvm.middleware.javaLangStringClassInfo


sealed class VerificationTypeInfo(val type: Type) {
  enum class Type { TOP, BOOLEAN, INTEGER, OBJECT, DOUBLE, EMPTY, NULL }

  override fun toString(): String {
    return type.toString()
  }

  val tag: Byte
    get() = when (type) {
      Type.TOP -> 0.toByte()
      Type.BOOLEAN -> 1.toByte()
      Type.INTEGER -> 1.toByte()
      Type.OBJECT -> 7.toByte()
      Type.DOUBLE -> 3.toByte()
      Type.EMPTY -> 0.toByte()
      Type.NULL -> 5.toByte()
    }
}

// VTI stands for Verification Type Info
class TopVti : VerificationTypeInfo(Type.TOP)
class IntegerVti : VerificationTypeInfo(Type.INTEGER)

class ObjectVti(
  val classInfo: ClassInfo,
  val isArray: Boolean
) : VerificationTypeInfo(Type.OBJECT) {
  constructor(classInfo: ClassInfo) : this(classInfo, isArray = false)

  override fun toString(): String {
    return "ObjectVti[${classInfo.className.encodedString}]"
  }
}

class BooleanVti : VerificationTypeInfo(Type.BOOLEAN)

class DoubleVti : VerificationTypeInfo(Type.DOUBLE)

class EmptyVti : VerificationTypeInfo(Type.EMPTY)

class NullVariableVti : VerificationTypeInfo(Type.NULL)

val javaLangStringObjectVti = ObjectVti(javaLangStringClassInfo, isArray = false)
