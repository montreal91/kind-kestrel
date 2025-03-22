package org.example.rlc.frontend.scope

sealed class VariableResolutionResult

class LocalVariable(val variableArrayIndex: Int) : VariableResolutionResult()
class GlobalVariable(val name: String): VariableResolutionResult()

// It actually can enclose over another enclosed variable
class EnclosedVariable(val name: String, val variableArrayIndex: Int) : VariableResolutionResult()
data object UnresolvedVariable : VariableResolutionResult()
