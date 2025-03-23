package org.example.rlc.frontend

import kotlin.math.max

internal class FrameStack {
  private val frameStack = ArrayDeque<Frame>()
  private val currentFunctionDepth = ArrayDeque<Int>()

  init {
    frameStack.addLast(Frame(type = Frame.Type.GLOBAL, index = 0))
  }

  internal fun addNewFrame(type: Frame.Type) {
    val newIndex = when (type) {
      Frame.Type.GLOBAL -> 1
      Frame.Type.BLOCK -> frameStack.last().getIndex()
      Frame.Type.FUNCTION -> 0
    }

    if (type == Frame.Type.FUNCTION) {
      currentFunctionDepth.addLast(frameStack.size)
    }

    frameStack.addLast(Frame(type, newIndex))
  }

  internal fun popFrame() {
    if (frameStack.last().isFunction()) {
      currentFunctionDepth.removeLast()
    }

    frameStack.removeLast()
  }

  internal fun existInCurrentFrame(identifier: String) = frameStack.last().containsIdentifier(identifier)

  internal fun declareVariable(identifier: String) = frameStack.last().declareVariable(identifier)


  internal fun isGlobal() = frameStack.last().isGlobal()

  internal fun existInAllFrames(identifier: String): Boolean {
    for (frame in frameStack) {
      if (frame.containsIdentifier(identifier)) {
        return true
      }
    }

    return false
  }

  internal fun lookup(identifier: String): LookupResult {
    var currentDepth = max(a = frameStack.size - 1, b = 0)

    for (frame in frameStack.reversed()) {
      if (frame.containsIdentifier(identifier)) {
        return LookupResult(
          index = frame.getResolvedIndex(identifier),
          isClosure = currentDepth < getCurrentFunctionDepth(),
          depth = currentDepth,
        )
      }

      currentDepth--
      currentDepth = max(a = currentDepth, b = 0)
    }

    throw IllegalArgumentException(
      "Identifier $identifier is not found in the frame stack. This should not happen."
    )
  }

  private fun getCurrentFunctionDepth() = when (currentFunctionDepth.isNotEmpty()) {
    true -> currentFunctionDepth.last()
    false -> 0
  }
}
