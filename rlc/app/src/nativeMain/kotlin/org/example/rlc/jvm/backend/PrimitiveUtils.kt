package org.example.rlc.jvm.backend

import java.io.File // TODO: Use Kotlin Native instead

fun Byte.toBytes() = listOf(this)

fun ByteArray.toShort() = ((this[0].toInt() shl 8) or (this[1].toInt() and 0xff)).toShort()

fun ByteArray.writeToFile(filePathName: String) {
  val file = File("$filePathName.class")
  file.writeBytes(array = this)
}

fun Short.toBytes(): List<Byte> {
  val res = mutableListOf<Byte>()
  res.add((this.toInt() shr 8).toByte())
  res.add(this.toByte())
  return res
}

fun Int.toBytes(): List<Byte> {
  val result = mutableListOf<Byte>()
  result.add((this shr 24).toByte())
  result.add((this shr 16).toByte())
  result.add((this shr 8).toByte())
  result.add(this.toByte())
  return result
}

fun Long.toBytes(): List<Byte> = List(size = 8) {
  i -> (this shr (i * 8) and 0xFF).toByte()
}

fun Double.toBytes(): List<Byte> = this.toBits().toBytes()
