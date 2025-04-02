package org.example.rlc.frontend

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Frame(private val type: Type, private var index: Int) {
  enum class Type {
    GLOBAL, BLOCK, FUNCTION
  }

  private val frameVariables = mutableMapOf<String, FrameVariable>()

  internal fun containsIdentifier(identifier: String) = frameVariables.containsKey(identifier)
  internal fun getIndex() = index
  internal fun isGlobal() = type == Type.GLOBAL
  internal fun isFunction() = type == Type.FUNCTION

  internal fun declareVariable(identifier: String, declarationId: Uuid) {
    println("Declared variable: $identifier. Resolved index: $index")
    frameVariables[identifier] = FrameVariable(index = index, isUpvalue = false, declarationId = declarationId)
    index++
  }

  internal fun markAsUpvalue(identifier: String) {
    if (!frameVariables.containsKey(identifier)) {
      return
    }

    frameVariables[identifier]!!.isUpvalue = true
  }

  internal fun getResolvedVariable(identifier: String): FrameVariable {
    return frameVariables.getOrElse(identifier, this::unresolvedIndex)
  }

  private fun unresolvedIndex(): FrameVariable {
    return FrameVariable(index = -1, isUpvalue = false, Uuid.NIL)
  }
}
