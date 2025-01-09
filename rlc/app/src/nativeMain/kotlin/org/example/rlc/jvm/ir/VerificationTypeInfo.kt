package org.example.rlc.jvm.ir

import org.example.rlc.jvm.middleware.javaLangStringClassInfo


sealed class VerificationTypeInfo(val type: Type) {
  enum class Type { TOP, BOOLEAN, INTEGER, OBJECT, DOUBLE, EMPTY }
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
    return "ObjectVti[${classInfo.className.encodedString}, isArray=${isArray}]"
  }
}

class BooleanVti : VerificationTypeInfo(Type.BOOLEAN)

class DoubleVti : VerificationTypeInfo(Type.DOUBLE)

class EmptyVti : VerificationTypeInfo(Type.EMPTY)

val javaLangStringObjectVti = ObjectVti(javaLangStringClassInfo, isArray = false)
