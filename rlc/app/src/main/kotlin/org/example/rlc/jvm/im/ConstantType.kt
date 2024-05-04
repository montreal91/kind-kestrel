package org.example.rlc.jvm.im

enum class ConstantType(val value: Byte) {
  // Values are stored alphabetically by type name
  CLASS(7.toByte()),
  FIELD_REF(9.toByte()),
  METHOD_REF(10.toByte()),
  NAME_AND_TYPE(12.toByte()),
  STRING(8.toByte()),
  UTF_8(1.toByte()),
}