package org.example.rlc.frontend

sealed class EnclosedSomething

class EnclosedLocal(val localVariableIndex: Int): EnclosedSomething() {
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

class EnclosedField(val fieldName: String): EnclosedSomething()
