package org.example.rlc.frontend

import co.touchlab.kermit.Logger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class FrameStack {
  private val frameStack = ArrayDeque<Frame>()
  private val currentFunctionDepth = ArrayDeque<Int>()

  private val log = FrameStack::class.qualifiedName?.let { Logger.withTag(it) }

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

  internal fun popFrame(): Frame {
    if (frameStack.last().isFunction()) {
      currentFunctionDepth.removeLast()
    }

    return frameStack.removeLast()
  }

  internal fun existInCurrentFrame(identifier: String) = frameStack.last().containsIdentifier(identifier)

  internal fun declareVariable(
    identifier: String, declarationId: Uuid
  ) {
    println("Declaring a variable (identifier=$identifier declarationId=$declarationId)")
    frameStack.last().declareLocalVariable(identifier, declarationId)
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
    println("recursiveLookup($identifier, $frameIndex)")
    val frame = frameStack[frameIndex]

    if (frame.containsIdentifier(identifier)) {
      val frameVariable = frame.getResolvedVariable(identifier)

      return LookupResult(
        index = frameVariable.index,
        variableType = when (frame.functionDepth < frameStack.last().functionDepth) {
          false -> VariableType.LOCAL
          true -> VariableType.CAPTURED_LOCAL
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

      val capturedType = when (lookup.variableType) {
        VariableType.CAPTURED_LOCAL -> VariableType.CAPTURED_UPVALUE
        VariableType.CAPTURED_UPVALUE -> VariableType.CAPTURED_UPVALUE
        VariableType.LOCAL -> VariableType.CAPTURED_LOCAL
      }

      frame.declareVariable(
        identifier = identifier,
        declarationId = lookup.declarationId,
        variableType = capturedType,
        index = lookup.index
      )

      return LookupResult(
        index = lookup.index,
        variableType = VariableType.CAPTURED_UPVALUE,
        isUpvalue = lookup.isUpvalue,
        depth = frameIndex,
        declarationId = lookup.declarationId,
      ) // This should be another lookup, based on which we will create a different enclosed object
    }

    if (frame.type == Frame.Type.FUNCTION && frame.functionDepth == frameStack.last().functionDepth) {
      println("Probably, need to add enclosed variable to a function here.")
      println("(Lookup = $lookup, identifier = $identifier, index = $frameIndex)")
      frame.declareVariable(
        identifier = identifier,
        declarationId = lookup.declarationId,
        variableType = lookup.variableType,
        index = lookup.index
      )
    }

    return lookup
  }
}
