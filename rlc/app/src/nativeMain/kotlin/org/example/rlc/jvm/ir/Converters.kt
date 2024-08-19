package org.example.rlc.jvm.ir

fun String.toUtf8Value() = Utf8Value(
  label = this,
  size = this.length.toShort(),
  value = this.encodeToByteArray(),
  encodedString = this,
)
