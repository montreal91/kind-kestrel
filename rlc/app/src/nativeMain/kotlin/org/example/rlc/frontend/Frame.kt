package org.example.rlc.frontend

class Frame(private val type: Type, private var index: Int) {
  enum class Type {
    GLOBAL, BLOCK, FUNCTION
  }

  private val frameVariables = mutableMapOf<String, Int>()

  internal fun containsIdentifier(identifier: String) = frameVariables.containsKey(identifier)
  internal fun getIndex() = index
  internal fun isGlobal() = type == Type.GLOBAL
  internal fun isFunction() = type == Type.FUNCTION

  internal fun declareVariable(identifier: String) {
    println("Declared variable: $identifier. Resolved index: $index")
    frameVariables[identifier] = index
    index++
  }

  internal fun getResolvedIndex(identifier: String): Int {
    return frameVariables.getOrElse(identifier, this::unresolvedIndex)
  }

  private fun unresolvedIndex(): Int {
    return -1
  }
}
