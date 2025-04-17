package org.example.rlc.frontend

import co.touchlab.kermit.Logger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Frame(
  val type: Type,
  private var index: Int,
  private val name: String,
  val functionDepth: Int
) {
  enum class Type {
    GLOBAL,
    BLOCK,
    FUNCTION,
    CLASS,
  }

  private val log = Frame::class.qualifiedName?.let { Logger.withTag(it) }

  override fun toString(): String {
    val sb = StringBuilder()
    for (variable in frameVariables.keys) {
      sb.append("$variable ")
    }

    return "(Frame type=$type name=$name functionDepth=$functionDepth variables=($sb))"
  }

  private val frameVariables = mutableMapOf<String, FrameVariable>()

  internal fun containsIdentifier(identifier: String) = frameVariables.containsKey(identifier)
  internal fun getIndex() = index
  internal fun isGlobal() = type == Type.GLOBAL
  internal fun isFunction() = type == Type.FUNCTION

  internal fun declareVariable(
    identifier: String,
    declarationId: Uuid,
    variableType: VariableType,
    index: Int
  ) {
    log?.d(messageString = "Declared variable: $identifier. Resolved index: $index. Variable type: $variableType")

    if (frameVariables.containsKey(identifier)) {
      log?.d(messageString = "Frame ($name) already contains identifier ($identifier)")
      return
    }

    frameVariables[identifier] = FrameVariable(
      index = index,
      isUpvalue = false,
      declarationId = declarationId,
      type = variableType
    )
  }

  internal fun declareLocalVariable(identifier: String, declarationId: Uuid) {
    log?.d(messageString = "Declared local variable: $identifier. Resolved index: $index")
    frameVariables[identifier] = FrameVariable(
      index = index,
      isUpvalue = false,
      declarationId = declarationId,
      type = VariableType.LOCAL
    )

    index++
  }

  internal fun getVariables() = frameVariables.toMap()

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
    return FrameVariable(index = -1, isUpvalue = false, Uuid.NIL, VariableType.LOCAL)
  }
}
