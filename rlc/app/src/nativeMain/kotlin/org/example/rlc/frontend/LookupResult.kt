package org.example.rlc.frontend

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class LookupResult(
  val index: Int,
  val variableType: VariableType,
  val depth: Int,
  val declarationId: Uuid,
  val isUpvalue: Boolean,
)

enum class VariableType {
  CAPTURED_LOCAL,
  CAPTURED_UPVALUE,
  LOCAL,
}
