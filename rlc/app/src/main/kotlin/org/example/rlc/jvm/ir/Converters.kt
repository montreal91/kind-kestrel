package org.example.rlc.jvm.ir

fun String.toUtf8Value() = Utf8Value(
  label = this,
  size = this.length.toShort(),
  value = this.toByteArray(Charsets.UTF_8),
)

fun String.toClassInfo() = ClassInfo(label = this + "_classInfo", className = this.toUtf8Value())
