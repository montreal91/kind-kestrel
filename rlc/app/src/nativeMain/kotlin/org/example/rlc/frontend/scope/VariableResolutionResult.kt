package org.example.rlc.frontend.scope

import org.example.rlc.frontend.EnclosedSomething

sealed class VariableResolutionResult

class LocalVariable(val variableArrayIndex: Int) : VariableResolutionResult()
class GlobalVariable(val name: String): VariableResolutionResult()

// It actually can enclose over another enclosed variable
class EnclosedVariable(val name: String, val enclosedObject: EnclosedSomething, val depth: Int) : VariableResolutionResult()
data object UnresolvedVariable : VariableResolutionResult()
