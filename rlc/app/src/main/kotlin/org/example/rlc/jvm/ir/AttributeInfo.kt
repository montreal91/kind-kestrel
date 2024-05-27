package org.example.rlc.jvm.ir

sealed class AttributeInfo(val attributeName: Utf8Value)


sealed class StackMapFrame(val tag: Byte) {
  abstract fun toBytes(): List<Byte>
}


class SameFrame(tag: Byte) : StackMapFrame(tag) {
  override fun toBytes(): List<Byte> {
    return listOf(tag)
  }
}

class StackMapTableAttribute(
  val frames: List<StackMapFrame>
) : AttributeInfo(stackMapTableAttributeName)


class CodeAttribute(
  val maxStack: Short,
  val maxLocals: Short,
  val code: List<Operation>,
  val exceptionTable: Any, // Replace with actual type later
  private val attributes: List<AttributeInfo>,
): AttributeInfo(codeAttributeName) {
  private val computedAttributes = mutableListOf<AttributeInfo>()

  val allAttributes: List<AttributeInfo> get() = attributes + computedAttributes
  fun addAttribute(attribute: AttributeInfo) = computedAttributes.add(attribute)
}
