package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toUtf8Value


/***
 * The intermediate representation that corresponds the following Java code
 *
 * ```
 * class LoxRuntimeError extends RuntimeException {
 *   LoxRuntimeError(String message) {
 *     super(message);
 *   }
 * }
 * ```
 */
internal fun loxRuntimeError() = ClassFile(
  thisClassInfo = loxRuntimeErrorClassInfo,
  superClassInfo = runtimeExceptionClassInfo(),
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  fieldList = listOf(),
  methodList = listOf(constructor()),
  interfaceList = listOf(),
  attributeList = listOf(),
)

private fun runtimeExceptionClassInfo() = ClassInfo(className = "java/lang/RuntimeException")

private fun constructor(): MethodInfo {
  val runtimeException = MethodRefInfo(
    label = "java/lang/RuntimeException.\"<init>\":(Ljava/lang/String;)V",
    classInfo = runtimeExceptionClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)V".toUtf8Value()
    ),
    argsSize = 2,
    returnSize = 0,
    returnTypeInfo = EmptyVti()
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeException),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2

  )

  return MethodInfo(
    methodName = constructorMethodName,
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(JavaString.VERIFICATION_TYPE), EmptyVti())
  )
}
