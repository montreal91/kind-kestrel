package org.example.rlc.frontend.scope

sealed class VariableResolutionResult

class LocalVariable(val variableArrayIndex: Int) : VariableResolutionResult()
data object UnresolvedVariable : VariableResolutionResult()
