package org.example.rlc.frontend

/**
 * Represents a sealed class for objects that are enclosed or conceptually grouped
 * within a certain structure or scope.
 *
 * Classes extending `EnclosedObject` are specialized to represent specific types
 * of enclosed entities or elements within a given context. This structure allows
 * for flexibility and extensibility in handling different kinds of enclosed objects.
 *
 * The usage of a sealed class ensures that all subclasses are defined in the same
 * file, providing exhaustive compile-time checks when working with the subclasses.
 */
sealed class EnclosedObject

class EnclosedLocal(val localVariableIndex: Int): EnclosedObject() {
  override fun toString(): String {
    return "(EnclosedLocal localVariableIndex=$localVariableIndex)"
  }

  override fun equals(other: Any?): Boolean {
    if (other is EnclosedLocal) {
      return localVariableIndex == other.localVariableIndex
    }

    return super.equals(other)
  }

  override fun hashCode(): Int {
    return toString().hashCode()
  }
}

class EnclosedUpvalue(val variableName: String): EnclosedObject() {
  override fun toString(): String {
    return "(EnclosedUpvalue variableName=$variableName)"
  }

  override fun equals(other: Any?): Boolean {
    if (other is EnclosedUpvalue) {
      return variableName == other.variableName
    }

    return super.equals(other)
  }

  override fun hashCode(): Int {
    return toString().hashCode()
  }
}
