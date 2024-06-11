package org.example.rlc.jvm.backend

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

fun Byte.toBytes() = listOf(this)

fun ByteArray.toShort() = ((this[0].toInt() shl 8) or (this[1].toInt() and 0xff)).toShort()

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.writeToFile(filePathName: String) {
  val file = fopen(
    _Filename = "$filePathName.class",
    _Mode = "wb"
  ) ?: throw IllegalArgumentException("Cannot open file at $filePathName")

  try {
    this.usePinned { pinned ->
      fwrite(pinned.addressOf(index = 0), 1.convert(), this.size.convert(), file)
    }
  } finally {
    fclose(file)
  }
}

fun Short.toBytes(): List<Byte> {
  val res = mutableListOf<Byte>()
  res.add((this.toInt() shr 8).toByte())
  res.add(this.toByte())
  return res
}

internal fun Int.toBytes(): List<Byte> {
  val result = mutableListOf<Byte>()
  result.add((this shr 24).toByte())
  result.add((this shr 16).toByte())
  result.add((this shr 8).toByte())
  result.add(this.toByte())
  return result
}

internal fun UInt.toBytes(): List<Byte> {
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

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toUtf8String(): String {
  return this.usePinned {
    it.addressOf(index = 0).toKString()
  }
}
