package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toUtf8Value

internal fun loxPointerCf() = ClassFile(
  thisClassInfo = ClassInfo(className = "LoxPointer"),
  superClassInfo = loxObjectClassInfo,
  accessFlagList = listOf(),
  attributeList = listOf(),
  fieldList = listOf(
    valueFieldInfo(),
  ),
  methodList = listOf(
    loxPointerConstructor(),
  ),
  interfaceList = listOf(),
)

private fun loxPointerConstructor(): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(
      Opcode.OP_GETSTATIC,
      generateLoxClassFieldRef(fieldName = "LOX_FUNCTION_CLASS"),
      loxObjectVti
    ),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_RETURN)
  )

  return MethodInfo(
    methodName = "<init>",
    accessFlagList = listOf(),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 1)),
    isStatic = false,
    signature = MethodSignature(listOf(), EmptyVti()),
  )
}

private fun valueFieldInfo() = FieldInfo(
  accessFlagList = emptyList(),
  fieldName = "__value__".toUtf8Value(),
  fieldDescriptor = "LLoxObject;".toUtf8Value(),
)
