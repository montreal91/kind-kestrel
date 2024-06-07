package org.example.rlc.jvm.backend

internal fun MutableList<Byte>.overwriteShort(start: Int, value: Short) {
  val bytes = value.toBytes()
  this[start] = bytes[0]
  this[start + 1] = bytes[1]
}
