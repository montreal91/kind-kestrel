package org.example.rlc.jvm.ir

class StackMapTableAttribute(
  val frames: List<StackMapFrame>
) : AttributeInfo(stackMapTableAttributeName) {
  fun isEmpty() = frames.isEmpty()

//  fun toBytes() = listOf<Byte>()
}
