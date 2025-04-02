package org.example.rlc.frontend

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class LookupResult(
  val index: Int,
  val isClosure: Boolean,
  val depth: Int,
  val declarationId: Uuid,
  val isUpvalue: Boolean,
)
