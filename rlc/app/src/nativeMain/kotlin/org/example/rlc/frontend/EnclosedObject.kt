package org.example.rlc.frontend

sealed class EnclosedObject

class EnclosedLocal(val localVariableIndex: Int): EnclosedObject() {
  override fun toString(): String {
    return "(EnclosedLocal (localVariableIndex=$localVariableIndex))"
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
