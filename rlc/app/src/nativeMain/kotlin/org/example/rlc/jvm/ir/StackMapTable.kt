package org.example.rlc.jvm.ir

sealed class StackMapFrame(val tag: Byte) {
  abstract fun toBytes(): List<Byte>
}

class SameFrame(tag: Byte) : StackMapFrame(tag) {
  override fun toBytes() = listOf(tag)
}

class FullFrame(
  tag: Byte,
  val offsetDelta: Byte,
  locals: List<VerificationTypeInfo>,
  stack: List<VerificationTypeInfo>,
) : StackMapFrame(tag) {
  private val _locals = locals.toMutableList()
  private val _stack = stack.toMutableList()

  val locals: List<VerificationTypeInfo> get() = _locals.toList()
  val stack: List<VerificationTypeInfo> get() = _stack.toList()

  override fun toBytes() = listOf(tag)
}

class StackMapTableAttribute(
  val frames: List<StackMapFrame>
) : AttributeInfo(stackMapTableAttributeName) {
  fun isEmpty() = frames.isEmpty()
}
