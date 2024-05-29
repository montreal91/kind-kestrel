package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.toClassInfo
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
  thisClassInfo = "LoxRuntimeError".toClassInfo(),
  superClassInfo = runtimeExceptionClassInfo(),
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  fieldList = listOf(),
  methodList = listOf(constructor()),
  interfaceList = listOf(),
  attributeList = listOf(),
)

private fun runtimeExceptionClassInfo() = "java/lang/RuntimeException".toClassInfo()

private fun constructor(): MethodInfo {
  val runtimeException = MethodRefInfo(
    label = "java/lang/RuntimeException.\"<init>\":(Ljava/lang/String;)V",
    classInfo = runtimeExceptionClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":(Ljava/lang/String;)V",
      name = "<init>".toUtf8Value(),
      descriptor = "(Ljava/lang/String;)V".toUtf8Value()
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, runtimeException),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(
    maxStack = 2,
    maxLocals = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "<init>".toUtf8Value(),
    methodDescriptor = "(Ljava/lang/String;)V".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributes = listOf(codeAttribute)
  )
}
