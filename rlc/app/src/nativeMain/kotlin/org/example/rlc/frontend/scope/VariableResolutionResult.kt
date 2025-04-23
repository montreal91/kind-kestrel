package org.example.rlc.frontend.scope

import org.example.rlc.frontend.EnclosedLocal
import org.example.rlc.frontend.EnclosedObject
import org.example.rlc.frontend.EnclosedUpvalue

sealed class VariableResolutionResult {
  abstract fun actualIndex(): Int
}

class LocalVariable(
  val name: String,
  val variableArrayIndex: Int,
  val isUpValue: Boolean,
) : VariableResolutionResult() {
  constructor(name: String, variableArrayIndex: Int) : this(
    name = name, variableArrayIndex = variableArrayIndex, isUpValue = false
  )

  override fun equals(other: Any?): Boolean {
    if (other is LocalVariable) {
      return variableArrayIndex == other.variableArrayIndex &&
          isUpValue == other.isUpValue &&
          name == other.name
    }

    return super.equals(other)
  }

  override fun hashCode(): Int {
    return toString().hashCode()
  }

  override fun toString(): String {
    return "(LocalVariable name=$name index=$variableArrayIndex isUpValue=$isUpValue)"
  }

  override fun actualIndex(): Int {
    return variableArrayIndex + 1
  }
}

class GlobalVariable(val name: String): VariableResolutionResult() {
  override fun equals(other: Any?): Boolean {
    if (other is GlobalVariable) {
      return this.name == other.name
    }

    return super.equals(other)
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun toString(): String {
    return "(GlobalVariable name=$name)"
  }

  override fun actualIndex(): Int {
    return -1
  }
}

class EnclosedVariable(
  val name: String,
  val enclosedObject: EnclosedObject,
  val depth: Int
) : VariableResolutionResult() {
  override fun equals(other: Any?): Boolean {
    if (other is EnclosedVariable) {
      return name == other.name && depth == other.depth && enclosedObject == other.enclosedObject
    }

    return super.equals(other)
  }

  override fun hashCode(): Int {
    return toString().hashCode()
  }

  override fun toString(): String {
    return "(EnclosedVariable name=$name depth=$depth enclosedObject=$enclosedObject)"
  }

  override fun actualIndex(): Int {
    return when (enclosedObject) {
      is EnclosedLocal -> enclosedObject.localVariableIndex + 1
      is EnclosedUpvalue -> -1
    }
  }
}

data object UnresolvedVariable : VariableResolutionResult() {
  override fun actualIndex(): Int {
    return -1
  }
}
