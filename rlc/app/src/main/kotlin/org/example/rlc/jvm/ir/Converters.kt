package org.example.rlc.jvm.ir

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

private fun doubleToJvmConstant(value: Double): ByteArray {
  val bits = value.toBits() // Convert the double to its raw bits
  return ByteArray(8) { i -> ((bits shr (56 - i * 8)) and 0xFF).toByte() }
}

fun Double.toDoubleValue() = DoubleValue(
  label = this.toString(),
  value = doubleToJvmConstant(value = this)
)
