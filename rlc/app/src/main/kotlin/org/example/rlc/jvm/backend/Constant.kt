package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ConstantType

class Constant(private val type: ConstantType, private val size: Short, private val value: ByteArray) {
  fun toBytes(): List<Byte> {
    val res = mutableListOf(type.value)
    if (type == ConstantType.UTF_8) {
      res.addAll(size.toBytes())
    }
    res.addAll(value.toList())
    return res
  }

  override fun toString(): String = when (type) {
    ConstantType.UTF_8 -> "Constant UTF-8: [" + this.value.toString(Charsets.UTF_8) + "]"
    ConstantType.CLASS -> "Constant Class: [" + this.value.toShort() + "]"
    ConstantType.FIELD_REF -> "Constant Field: [" + this.value.toShort() + " " + this.value.copyOfRange(2, 4)
      .toShort() + "]"

    ConstantType.METHOD_REF -> "Constant MethodRef: [" + this.value.toShort() + " " + this.value.copyOfRange(2, 4)
      .toShort() + "]"

    ConstantType.NAME_AND_TYPE -> "Constant NameAndType: [" + this.value.toShort() + " " + this.value.copyOfRange(2, 4)
      .toShort() + "]"

    ConstantType.STRING -> "Constant String: [" + this.value.toShort() + "]"
  }
}
