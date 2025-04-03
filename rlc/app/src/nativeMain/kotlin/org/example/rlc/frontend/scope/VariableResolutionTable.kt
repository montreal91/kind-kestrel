package org.example.rlc.frontend.scope

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class VariableResolutionTable {
  private val table = mutableMapOf<Uuid, VariableResolutionResult>()

  fun get(id: Uuid) = table[id] ?: UnresolvedVariable

  fun getMaxIndex() = table.values
    .filterIsInstance<LocalVariable>()
    .maxOfOrNull { it.variableArrayIndex } ?: 0

  fun set(id: Uuid, resolution: VariableResolutionResult) {
    if (table.containsKey(id)) {
      throw IllegalStateException("This identifier node is already resolved: ${table[id]}")
    }

    table[id] = resolution
  }

  fun updateAsUpvalue(id: Uuid) {
    if (!table.containsKey(id)) {
      throw IllegalStateException("This identifier is not declared yet: $id")
    }

    val resolution = table[id]!!

    table[id] = when (resolution) {
      is EnclosedVariable -> resolution
      is GlobalVariable -> resolution
      is LocalVariable -> LocalVariable(
        resolution.variableArrayIndex,
        isUpValue = true,
      )
      UnresolvedVariable -> resolution
    }
  }

  fun _test_getKeys() = table.keys
}
