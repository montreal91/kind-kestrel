package org.example.rlc.jvm.ir


sealed class VerificationTypeInfo(val type: Type) {
  enum class Type { TOP, INTEGER, OBJECT, DOUBLE }
}

class TopVti : VerificationTypeInfo(Type.TOP)
class IntegerVti : VerificationTypeInfo(Type.INTEGER)

class ObjectVti(
  val classInfo: ClassInfo,
  val isArray: Boolean
) : VerificationTypeInfo(Type.OBJECT) {
  constructor(classInfo: ClassInfo) : this(classInfo, isArray = false)
}

class DoubleVti : VerificationTypeInfo(Type.DOUBLE)
