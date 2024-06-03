package org.example.rlc.jvm.ir

enum class ClassAccessFlags(val value: Short) {
  PUBLIC(value = 0x0001),
  SUPER(value = 0x0020),
}

enum class MethodAccessFlags(val value: Short) {
  FINAL(value = 0x0010),
  NONE(value = 0x0000),
  PRIVATE(value = 0x0002),
  PUBLIC(value = 0x0001),
  STATIC(value = 0x0008),
}

enum class FieldAccessFlags(val value: Short) {
  FINAL(value = 0x0010),
  STATIC(value = 0x0008),
}
