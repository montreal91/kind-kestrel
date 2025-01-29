package org.example.rlc.frontend

internal class FrameStack {
  private val frameStack = ArrayDeque<MutableMap<String, Int>>()
  private var index = 0

  init {
    frameStack.addLast(mutableMapOf())
  }

  internal fun addNewFrame() = frameStack.addLast(mutableMapOf())

  internal fun popFrame() = frameStack.removeLast()

  internal fun existInCurrentFrame(identifier: String)
      = frameStack.last().containsKey(identifier)

  internal fun declareVariable(identifier: String) {
    frameStack.last()[identifier] = index
    index++
  }

  internal fun existInAllFrames(identifier: String): Boolean {
    for (frame in frameStack) {
      if (frame.containsKey(identifier)) {
        return true
      }
    }

    return false
  }

  internal fun lookup(identifier: String): Int {
    for (frame in frameStack.reversed()) {
      if (frame.containsKey(identifier)) {
        return frame[identifier]!!
      }
    }

    throw IllegalArgumentException(
      "Identifier $identifier is not found in the frame stack. This should not happen."
    )
  }
}
