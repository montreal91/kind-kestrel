package org.example.rlc.jvm.im

enum class ClassAccessFlags(val value: Short) {
  PUBLIC(value = 0x0001),
}

enum class MethodAccessFlags(val value: Short) {
  PUBLIC(value = 0x0001),
  STATIC(value = 0x0008),
}
