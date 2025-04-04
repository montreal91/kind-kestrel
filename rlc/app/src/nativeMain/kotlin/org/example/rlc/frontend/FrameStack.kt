package org.example.rlc.frontend

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class FrameStack {
  private val frameStack = ArrayDeque<Frame>()
  private val currentFunctionDepth = ArrayDeque<Int>()

  init {
    frameStack.addLast(Frame(type = Frame.Type.GLOBAL, index = 0, name = "LoxScript", functionDepth = 0))
  }

  override fun toString(): String {
    val sb = StringBuilder()
    for (frame in frameStack) {
      sb.append("\n  $frame")
    }

    return "(FrameStack:$sb\n)"
  }

  internal fun addNewFrame(type: Frame.Type, frameName: String) {
    val newIndex = when (type) {
      Frame.Type.GLOBAL -> 1
      Frame.Type.BLOCK -> frameStack.last().getIndex()
      Frame.Type.FUNCTION -> 0
    }

    val newDepth = when (type) {
      Frame.Type.GLOBAL -> 0
      Frame.Type.BLOCK -> frameStack.last().functionDepth
      Frame.Type.FUNCTION -> frameStack.last().functionDepth + 1
    }

    if (type == Frame.Type.FUNCTION) {
      currentFunctionDepth.addLast(frameStack.size)
    }

    frameStack.addLast(Frame(type, newIndex, frameName, newDepth))
  }

  internal fun popFrame() {
    if (frameStack.last().isFunction()) {
      currentFunctionDepth.removeLast()
    }

    frameStack.removeLast()
  }

  internal fun existInCurrentFrame(identifier: String) = frameStack.last().containsIdentifier(identifier)

  internal fun declareVariable(
    identifier: String, declarationId: Uuid
  ) {
    println("Declaring a variable (identifier=$identifier declarationId=$declarationId)")
    frameStack.last().declareVariable(identifier, declarationId)
  }

  internal fun isGlobal() = frameStack.last().isGlobal()

  internal fun existInAllFrames(identifier: String): Boolean {
    for (frame in frameStack) {
      if (frame.containsIdentifier(identifier)) {
        return true
      }
    }

    return false
  }

  internal fun markAsUpvalue(identifier: String) {
    println("Mark variable (identifier=$identifier) as upvalue.")
    for (frame in frameStack.reversed()) {
      if (frame.containsIdentifier(identifier)) {
        frame.markAsUpvalue(identifier)
      }
    }
  }

  internal fun lookup(identifier: String): LookupResult {
    return recursiveLookup(identifier, frameStack.lastIndex)
  }

  private fun recursiveLookup(identifier: String, frameIndex: Int): LookupResult {
    val frame = frameStack[frameIndex]

    if (frame.containsIdentifier(identifier)) {
      val frameVariable = frame.getResolvedVariable(identifier)

      return LookupResult(
        index = frameVariable.index,
        variableType = when (frame.functionDepth < frameStack.last().functionDepth) {
          false -> LookupResult.Type.LOCAL
          true -> LookupResult.Type.CAPTURED_LOCAL
        },
        isUpvalue = frameVariable.isUpvalue,
        depth = frameIndex,
        declarationId = frameVariable.declarationId,
      )
    }

    if (frameIndex == 0) {
      throw IllegalArgumentException(
        "Identifier $identifier is not found in the frame stack. This should not happen."
      )
    }

    // We actually need to do something here
    val lookup = recursiveLookup(identifier = identifier, frameIndex = frameIndex - 1)

    if (frame.type == Frame.Type.FUNCTION && frame.functionDepth < frameStack.last().functionDepth) {
      println("Why are we here? >>> ($identifier $lookup)")
      frame.declareVariable(identifier, declarationId = lookup.declarationId)

      return LookupResult(
        index = lookup.index,
        variableType = LookupResult.Type.CAPTURED_UPVALUE,
        isUpvalue = lookup.isUpvalue,
        depth = frameIndex,
        declarationId = lookup.declarationId,
      ) // This should be another lookup, based on which we will create a different enclosed object
    }

    return lookup
  }

  private fun getCurrentFunctionDepth() = when (currentFunctionDepth.isNotEmpty()) {
    true -> currentFunctionDepth.last()
    false -> 0
  }
}
