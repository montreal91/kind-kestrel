package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.toUtf8Value

internal fun generateLoxClassFieldRef(fieldName: String) = FieldRefInfo(
  label = "LoxObject.$fieldName:LLoxClass;",
  classInfo = loxObjectClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "$fieldName:LLoxClass;",
    name = fieldName.toUtf8Value(),
    descriptor = loxClassDescriptor,
  )
)
