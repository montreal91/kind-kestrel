package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.BooleanVti
import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.javaLangStringObjectVti
import org.example.rlc.jvm.ir.toUtf8Value

/***
 * The intermediate representation that corresponds the following Java code
 *
 * ```
 * class LoxClass {
 *   final String name;
 *
 *   LoxClass(String name) {
 *     this.name = name;
 *   }
 *
 *   @Override
 *   public boolean equals(Object other) {
 *     if (other instanceof LoxClass) {
 *       return name.equals(((LoxClass) other).name);
 *     }
 *
 *     return false;
 *   }
 * }
 * ```
 */
internal fun loxClass() = ClassFile(
  // This class feels redundant in the JVM environment.
  // But let's keep it until classes are actually implemented.
  thisClassInfo = loxClassInfo,
  superClassInfo = javaLangObjectClassInfo,
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  attributeList = listOf(),
  fieldList = listOf(finalStringName()),
  methodList = listOf(loxClassConstructor(), loxClassEqualsMethod()),
  interfaceList = listOf()
)


private fun finalStringName() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "name".toUtf8Value(),
  fieldDescriptor = javaStringDescriptor
)

private fun nameRef() = FieldRefInfo(
  label = "LoxClass.name:Ljava/lang/String;",
  classInfo = loxClassInfo,
  nameAndType = NameAndTypeInfo(
    label = "name:Ljava/lang/String;",
    name = "name".toUtf8Value(),
    descriptor = javaStringDescriptor,
  ),
)

private fun loxClassConstructor(): MethodInfo {
  val code = CodeAttribute(
    argsSize = 2,
    code = listOf(
      SimpleOperation(Opcode.OP_ALOAD_0),
      ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, objectConstructor),
      SimpleOperation(Opcode.OP_ALOAD_0),
      SimpleOperation(Opcode.OP_ALOAD_1),
      ShortConstantOperation(opcode = Opcode.OP_PUTFIELD, constant = nameRef()),
      SimpleOperation(Opcode.OP_RETURN)
    ),
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2
  )

  return MethodInfo(
    methodName = constructorMethodName,
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(code),
    isStatic = true,
    signature = MethodSignature(
      listOf(ObjectVti(classInfo = javaLangStringClassInfo, isArray = false)),
      EmptyVti()
    ),
  )
}

private fun loxClassEqualsMethod(): MethodInfo {
  val code = CodeAttribute(
    argsSize = 2,
    code = listOf(
      SimpleOperation(Opcode.OP_ALOAD_1),
      ShortConstantOperation(Opcode.OP_INSTANCEOF, loxClassInfo),
      ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 10),
      SimpleOperation(Opcode.OP_ALOAD_0),
      ShortConstantOperation(Opcode.OP_GETFIELD, nameRef(), javaLangStringObjectVti),
      SimpleOperation(Opcode.OP_ALOAD_1),
      ShortConstantOperation(Opcode.OP_CHECKCAST, loxClassInfo),
      ShortConstantOperation(Opcode.OP_GETFIELD, nameRef(), javaLangStringObjectVti),
      ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, stringEquals()),
      SimpleOperation(Opcode.OP_IRETURN),
      SimpleOperation(Opcode.OP_ICONST_0),
      SimpleOperation(Opcode.OP_IRETURN),
    ),
    exceptionTable = 0,
    attributes = listOf(),
    maxLocals2 = 2
  )

  return MethodInfo(
    methodName = "equals",
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(code),
    isStatic = false,
    signature = MethodSignature(
      listOf(ObjectVti(javaLangObjectClassInfo)),
      BooleanVti()
    )
  )
}
