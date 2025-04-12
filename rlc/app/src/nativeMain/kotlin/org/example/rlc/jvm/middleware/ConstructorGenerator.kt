package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation

internal fun generateConstructor(className: String): MethodInfo {
  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, generateConstructorMethodRef(className)),
    SimpleOperation(Opcode.OP_RETURN)
  )

  return MethodInfo(
    methodName = "<init>",
    accessFlagList = listOf(),
    attributeList = listOf(CodeAttribute(code = code, argsSize = 1)),
    isStatic = false,
    signature = MethodSignature(listOf(), EmptyVti())
  )
}

private fun generateConstructorMethodRef(className: String): MethodRefInfo {
  return MethodRefInfo(
    label = "$className.\"<init>\":()V",
    classInfo = ClassInfo(className),
    nameAndType = initializerNameAndType, // Maybe this also will require some parametrization
    argsSize = 1,
    returnSize = 0,
    returnTypeInfo = EmptyVti(),
  )
}
