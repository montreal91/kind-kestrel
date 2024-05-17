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
}

class MethodRefInfo(
  override val label: String,
  val classInfo: ClassInfo,
  val nameAndType: NameAndTypeInfo
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.METHOD_REF
}

class Utf8Value(
  label: String,
  value: ByteArray,
  val size: Short,
) : ConstantValue(type = ConstantType.UTF_8, label = label, value = value)

class NameAndTypeInfo(
  override val label: String,
  val name: Utf8Value,
  val descriptor: Utf8Value
) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.NAME_AND_TYPE
}

class StringRefInfo(override val label: String, val stringConstant: Utf8Value) : ConstantPoolInfo() {
  override val type: ConstantType
    get() = ConstantType.STRING
}
