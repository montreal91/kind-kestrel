package org.example.rlc.jvm.ir

internal class FullFrameBuilder {
  private var offsetDelta: Byte = 0
  private var locals: List<VerificationTypeInfo> = listOf()
  private var stack: List<VerificationTypeInfo> = listOf()
  private var needToBuild = false

  fun offsetDelta(value: Byte): FullFrameBuilder {
    offsetDelta = value
    return this
  }

  fun locals(locals: List<VerificationTypeInfo>): FullFrameBuilder {
    this.locals = locals
    return this
  }

  fun stack(stack: List<VerificationTypeInfo>): FullFrameBuilder {
    this.stack = stack
    return this
  }

  fun needToBuild(needToBuild: Boolean): FullFrameBuilder {
    this.needToBuild = needToBuild;
    return this
  }

  val toBuild
    get() = needToBuild

  fun build() = FullFrame(offsetDelta = offsetDelta, locals = locals, stack = stack)
}
