package org.example.rlc.frontend

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class FrameVariable(
  val index: Int,
  var isUpvalue: Boolean,
  val declarationId: Uuid,
  val type: VariableType,
)
