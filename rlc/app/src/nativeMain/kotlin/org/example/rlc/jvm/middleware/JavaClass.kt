package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.toUtf8Value

object JavaClass {
  fun generateFieldRef(className: String, fieldName: String): FieldRefInfo {
    return FieldRefInfo(
      label = "$className.$fieldName:LLoxObject;",
      classInfo = ClassInfo(className),
      nameAndType = NameAndTypeInfo(
        label = "$fieldName:LLoxObject;",
        name = fieldName.toUtf8Value(),
        descriptor = "LLoxObject;".toUtf8Value()
      )
    )
  }
}
