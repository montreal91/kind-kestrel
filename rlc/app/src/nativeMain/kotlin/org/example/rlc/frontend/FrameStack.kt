package org.example.rlc.frontend

internal class FrameStack {
  private val frameStack = ArrayDeque<Frame>()

  init {
    frameStack.addLast(Frame(type = Frame.Type.GLOBAL, index = 0))
  }

  internal fun addNewFrame(type: Frame.Type) {
    val newIndex = when(type) {
      Frame.Type.GLOBAL -> 1
      Frame.Type.BLOCK -> frameStack.last().getIndex()
      Frame.Type.FUNCTION -> 1
    }

    frameStack.addLast(Frame(type, newIndex))
  }

  internal fun popFrame() = frameStack.removeLast()

  internal fun existInCurrentFrame(identifier: String)
      = frameStack.last().containsIdentifier(identifier)

  internal fun declareVariable(identifier: String)
      = frameStack.last().declareVariable(identifier)


  internal fun isGlobal() = frameStack.last().isGlobal()

  internal fun existInAllFrames(identifier: String): Boolean {
    for (frame in frameStack) {
      if (frame.containsIdentifier(identifier)) {
        return true
      }
    }

    return false
  }

  internal fun lookup(identifier: String): Int {
    for (frame in frameStack.reversed()) {
      if (frame.containsIdentifier(identifier)) {
        return frame.getResolvedIndex(identifier)
      }
    }

    throw IllegalArgumentException(
      "Identifier $identifier is not found in the frame stack. This should not happen."
    )
  }
}
