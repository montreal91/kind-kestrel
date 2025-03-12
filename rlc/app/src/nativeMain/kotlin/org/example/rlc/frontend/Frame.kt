package org.example.rlc.frontend

class Frame(private val type: Type, private var index: Int) {
  enum class Type {
    GLOBAL, BLOCK, FUNCTION
  }

  private val frameVariables = mutableMapOf<String, Int>()

  init {
    if (type == Type.FUNCTION) {
      index = 1
    }
  }

  internal fun containsIdentifier(identifier: String) = frameVariables.containsKey(identifier)
  internal fun getIndex() = index
  internal fun isGlobal() = type == Type.GLOBAL
  internal fun isFunction() = type == Type.FUNCTION

  internal fun declareVariable(identifier: String) {
    frameVariables[identifier] = index
    index++
  }

  internal fun getResolvedIndex(identifier: String): Int {
    return frameVariables.getOrElse(identifier, this::default)
  }

  internal fun default(): Int {
    return -1
  }
}
