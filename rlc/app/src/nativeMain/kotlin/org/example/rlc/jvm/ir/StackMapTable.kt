package org.example.rlc.jvm.ir

class StackMapTableAttribute(
  val frames: List<StackMapFrame>
) : AttributeInfo(stackMapTableAttributeName)
