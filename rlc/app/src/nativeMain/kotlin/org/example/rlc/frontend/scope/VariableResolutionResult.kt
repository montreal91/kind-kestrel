package org.example.rlc.frontend.scope

sealed class VariableResolutionResult

class LocalVariable(val variableArrayIndex: Int) : VariableResolutionResult()
class GlobalVariable(val name: String): VariableResolutionResult()
data object UnresolvedVariable : VariableResolutionResult()
