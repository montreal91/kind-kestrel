package org.example.rlc.frontend

import org.example.rlc.jvm.im.ClassInfo
import org.example.rlc.jvm.im.Utf8Value

fun String.toUtf8Value() = Utf8Value(
  label = this,
  size = this.length.toShort(),
  value = this.toByteArray(Charsets.UTF_8),
)

fun String.toClassInfo() = ClassInfo(label = this + "_classInfo", className = this.toUtf8Value())
