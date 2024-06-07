package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.ConstantPoolInfo
import org.example.rlc.jvm.ir.ConstantValue
import org.example.rlc.jvm.ir.DoubleValue
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.StringRefInfo
import org.example.rlc.jvm.ir.Utf8Value


class ConstantPool {
  private val constants: MutableList<Constant> = mutableListOf()
  private val labelIndex: MutableMap<String, Int> = mutableMapOf()

  private var offset = 0

  private val size: Int
    get() = constants.size + 1 + offset

  operator fun get(label: String): Short = labelIndex[label]!!.toShort()

  fun addConstantPoolInfo(constantPoolInfo: ConstantPoolInfo) {
    if (labelIndex.containsKey(constantPoolInfo.label)) {
      return
    }

    when (constantPoolInfo) {
      is ClassInfo -> addClassInfo(constantPoolInfo)
      is ConstantValue -> addConstantValue(constantPoolInfo)
      is FieldRefInfo -> addFieldRefInfo(constantPoolInfo)
      is MethodRefInfo -> addMethodRefInfo(constantPoolInfo)
      is NameAndTypeInfo -> addNameAndTypeInfo(constantPoolInfo)
      is StringRefInfo -> addStringRefInfo(constantPoolInfo)
    }
  }

  fun toBytes(): List<Byte> {
    val res: MutableList<Byte> = mutableListOf()
    res.addAll(size.toShort().toBytes())

    for (constant in constants) {
      res.addAll(constant.toBytes())
    }

    return res
  }

  private fun addClassInfo(info: ClassInfo) {
    addConstantPoolInfo(info.className)
    addConstant(
      info.label,
      Constant(info.type, 1, getIndexAsByteArray(info.className.label))
    )
  }

  private fun addConstantValue(constantValue: ConstantValue) = when (constantValue) {
    is Utf8Value -> addConstant(
      constantValue.label,
      Constant(constantValue.type, constantValue.size, constantValue.value)
    )

    is DoubleValue -> addDoubleConstant(constantValue)

    else -> {
      throw IllegalArgumentException("Unexpected constant value type ${constantValue.value}")
    }
  }

  private fun addFieldRefInfo(info: FieldRefInfo) {
    addConstantPoolInfo(info.classInfo)
    addConstantPoolInfo(info.nameAndType)

    val constant = Constant(
      info.type,
      2,
      getTwoIndexesAsByteArray(info.classInfo.label, info.nameAndType.label)
    )
    addConstant(info.label, constant)
  }

  private fun addMethodRefInfo(info: MethodRefInfo) {
    addConstantPoolInfo(info.classInfo)
    addConstantPoolInfo(info.nameAndType)

    val constant = Constant(
      info.type,
      2,
      getTwoIndexesAsByteArray(info.classInfo.label, info.nameAndType.label)
    )
    addConstant(info.label, constant)
  }

  private fun addNameAndTypeInfo(info: NameAndTypeInfo) {
    addConstantPoolInfo(info.name)
    addConstantPoolInfo(info.descriptor)

    val value = getTwoIndexesAsByteArray(info.name.label, info.descriptor.label)
    addConstant(info.label, Constant(info.type, 1, value))
  }

  private fun addStringRefInfo(info: StringRefInfo) {
    addConstantPoolInfo(info.stringConstant)
    val value = getIndexAsByteArray(info.stringConstant.label)
    addConstant(info.label, Constant(info.type, 1, value))
  }

  private fun addConstant(label: String, constant: Constant) {
    constants.add(constant)
    labelIndex[label] = constants.size + offset
  }

  // This function assumes that label exists in labelIndex
  private fun getIndexAsByteArray(label: String): ByteArray {
    return this[label].toBytes().toByteArray()
  }

  private fun getTwoIndexesAsByteArray(label1: String, label2: String): ByteArray {
    val byteArray1 = getIndexAsByteArray(label1)
    val byteArray2 = getIndexAsByteArray(label2)

    val result = ByteArray(size = byteArray1.size + byteArray2.size)

    byteArray1.copyInto(result)
    byteArray2.copyInto(result, destinationOffset = byteArray1.size)

    return result
  }

  private fun addDoubleConstant(doubleValue: DoubleValue) {
    val constant = Constant(
      doubleValue.type,
      doubleValue.value.size.toShort(),
      doubleValue.value
    )

    addConstant(doubleValue.label, constant)
    offset += 1
  }
}
