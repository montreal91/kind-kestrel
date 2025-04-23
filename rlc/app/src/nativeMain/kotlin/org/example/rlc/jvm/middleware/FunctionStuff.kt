package org.example.rlc.jvm.middleware

import org.example.rlc.frontend.scope.EnclosedVariable

data class FunctionStuff(
  val name: String,
  val enclosedVariables: List<EnclosedVariable>
)
