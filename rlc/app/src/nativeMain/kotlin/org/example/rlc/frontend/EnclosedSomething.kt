package org.example.rlc.frontend

sealed class EnclosedSomething

class EnclosedLocal(val localVariableIndex: Int): EnclosedSomething()
class EnclosedField(val fieldName: String): EnclosedSomething()
