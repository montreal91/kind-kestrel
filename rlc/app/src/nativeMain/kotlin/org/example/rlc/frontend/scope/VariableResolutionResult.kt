package org.example.rlc.frontend.scope

import org.example.rlc.frontend.EnclosedSomething

sealed class VariableResolutionResult

class LocalVariable(
  val variableArrayIndex: Int,
  val isUpValue: Boolean,
) : VariableResolutionResult() {
  constructor(variableArrayIndex: Int) : this(
    variableArrayIndex = variableArrayIndex, isUpValue = false
  )

  override fun equals(other: Any?): Boolean {
    if (other is LocalVariable) {
      return variableArrayIndex == other.variableArrayIndex &&
          isUpValue == other.isUpValue
    }

    return super.equals(other)
  }

  override fun hashCode(): Int {
    return "$variableArrayIndex$isUpValue".hashCode()
  }

  override fun toString(): String {
    return "(LocalVariable index=$variableArrayIndex isUpValue=$isUpValue)"
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
}

// It actually can enclose over another enclosed variable
class EnclosedVariable(
  val name: String,
  val enclosedObject: EnclosedSomething,
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
}

data object UnresolvedVariable : VariableResolutionResult()
