package org.example.rlc.jvm.ir

import org.example.rlc.jvm.backend.toBytes

fun String.toUtf8Value() = Utf8Value(
  label = this,
  size = this.length.toShort(),
  value = this.toByteArray(Charsets.UTF_8),
)

fun String.toClassInfo() = ClassInfo(label = this + "_classInfo", className = this.toUtf8Value())

fun String.toStringRefInfo() = StringRefInfo(
  label = this + "_ref",
  stringConstant = this.toUtf8Value()
)

fun Double.toDoubleValue() = DoubleValue(label = this.toString(), value = this.toBytes().toByteArray())
