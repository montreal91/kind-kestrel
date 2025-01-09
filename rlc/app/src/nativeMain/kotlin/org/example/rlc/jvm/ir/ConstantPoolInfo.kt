package org.example.rlc.jvm.ir


sealed class ConstantPoolInfo {
  abstract val type: ConstantType
  abstract val label: String
  override fun toString() = "[$type] $label"
}

class ClassInfo(
  override val label: String,
  val className: Utf8Value,
) : ConstantPoolInfo() {

  constructor(className: String) : this(
    label = className + "_ClassInfo",
    className.toUtf8Value()
  )

  override val type: ConstantType
    get() = ConstantType.CLASS
}

abstract class ConstantValue(
  override val type: ConstantType,
  override val label: String,
  open val value: ByteArray
) : ConstantPoolInfo()

class FieldRefInfo(
  override val label: String,
  val classInfo: ClassInfo,
  val nameAndType: NameAndTypeInfo
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.FIELD_REF

  val size: Int get() = when {
    nameAndType.descriptor.label == "D" -> 2
    else -> 1
  }
}

class MethodRefInfo(
  override val label: String,
  val classInfo: ClassInfo,
  val nameAndType: NameAndTypeInfo,
  val argsSize: Int,
  val returnSize: Int,
  val returnTypeInfo: VerificationTypeInfo,
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.METHOD_REF
}

class DoubleValue(
  label: String,
  value: ByteArray,
) : ConstantValue(type = ConstantType.DOUBLE, label = label, value = value) {
  constructor(value: String) : this(
    label = value + "_DoubleValue",
    value = doubleToJvmConstant(value.toDouble())
  )
}

private fun doubleToJvmConstant(value: Double): ByteArray {
  val bits = value.toBits() // Convert the double to its raw bits
  return ByteArray(size = 8) { i -> ((bits shr (56 - i * 8)) and 0xFF).toByte() }
}


class Utf8Value(
  label: String,
  value: ByteArray,
  val encodedString: String,
  val size: Short,
) : ConstantValue(type = ConstantType.UTF_8, label = label, value = value) {

  constructor(str: String) : this(
    label = str,
    value = str.encodeToByteArray(),
    size = str.length.toShort(),
    encodedString = str,
  )
}

class NameAndTypeInfo(
  override val label: String,
  val name: Utf8Value,
  val descriptor: Utf8Value
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.NAME_AND_TYPE
}

class StringRefInfo(
  override val label: String,
  val stringConstant: Utf8Value
) : ConstantPoolInfo() {

  constructor(value: String) : this(
    label = value + "_StringRef",
    stringConstant = value.toUtf8Value()
  )

  override val type: ConstantType
    get() = ConstantType.STRING
}
