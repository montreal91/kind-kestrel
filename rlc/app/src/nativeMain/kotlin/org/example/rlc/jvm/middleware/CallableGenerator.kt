package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.Operation

internal fun generateLoxFunction(name: String, arity: Int, code: List<Operation>): ClassFile {
  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxFunction${name}"),
    superClassInfo = ClassInfo(className = "LoxCallable${arity}"),
    interfaceList = listOf(),
    methodList = listOf(),
    attributeList = listOf(),
    accessFlagList = listOf(ClassAccessFlags.ABSTRACT),
    fieldList = listOf()
  )
}

internal fun generateAbstractCallable(arity: Int): ClassFile {

  return ClassFile(
    thisClassInfo = ClassInfo(className = "LoxCallable${arity}"),
    superClassInfo = ClassInfo(className = "LoxBasicCallable"),
    interfaceList = listOf(),
    methodList = listOf(),
    attributeList = listOf(),
    accessFlagList = listOf(ClassAccessFlags.ABSTRACT),
    fieldList = listOf()
  )
}
