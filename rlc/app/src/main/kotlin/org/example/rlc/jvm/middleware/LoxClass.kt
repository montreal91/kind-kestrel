package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ControlFlowOperation
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.ShortConstantOperation
import org.example.rlc.jvm.ir.SimpleOperation
import org.example.rlc.jvm.ir.constructorMethodName
import org.example.rlc.jvm.ir.javaLangObjectClassInfo
import org.example.rlc.jvm.ir.javaStringDescriptor
import org.example.rlc.jvm.ir.objectConstructor
import org.example.rlc.jvm.ir.toClassInfo
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
  thisClassInfo = "LoxClass".toClassInfo(),
  superClassInfo = javaLangObjectClassInfo,
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  attributeList = listOf(),
  fieldList = listOf(finalStringName()),
  methodList = listOf(loxClassConstructor(), loxClassEqualsMethod()),
  interfaceList = listOf()
)

private fun loxClassInfo() = "LoxClass".toClassInfo()


private fun finalStringName() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "name".toUtf8Value(),
  fieldDescriptor = javaStringDescriptor
)

private fun nameRef() = FieldRefInfo(
  label = "LoxClass.name:Ljava/lang/String",
  classInfo = loxClassInfo(),
  nameAndType = NameAndTypeInfo(
    label = "name:Ljava/lang/String",
    name = "name".toUtf8Value(),
    descriptor = javaStringDescriptor,
  ),
)

private fun loxClassConstructor() = MethodInfo(
  methodName = constructorMethodName,
  methodDescriptor = "(Ljava/lang/String;)V".toUtf8Value(),
  maxStack = 2,
  maxLocals = 2,
  accessFlagList = listOf(MethodAccessFlags.NONE),
  code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, objectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(opcode = Opcode.OP_PUTFIELD, constant = nameRef()),
    SimpleOperation(Opcode.OP_RETURN)
  )
)

private fun stringEquals(): MethodRefInfo {
  val nameInfo = "equals".toUtf8Value()
  val typeInfo = "(Ljava/lang/Object;)Z".toUtf8Value()
  return MethodRefInfo(
    label = "java/lang/String.equals:(Ljava/lang/Object;)Z",
    classInfo = "String".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "equals:(Ljava/lang/Object;)Z",
      name = nameInfo,
      descriptor = typeInfo,
    )
  )
}

private fun loxClassEqualsMethod() = MethodInfo(
  methodName = "equals".toUtf8Value(),
  methodDescriptor = "(Ljava/lang/Object;)Z".toUtf8Value(),
  maxStack = 2,
  maxLocals = 2,
  accessFlagList = listOf(MethodAccessFlags.PUBLIC),
  code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_INSTANCEOF, loxClassInfo()),
    ControlFlowOperation(Opcode.OP_IFEQ, jumpTo = 10),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, nameRef()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxClassInfo()),
    ShortConstantOperation(Opcode.OP_GETFIELD, nameRef()),
    ShortConstantOperation(Opcode.OP_INVOKE_VIRTUAL, stringEquals()),
    SimpleOperation(Opcode.OP_IRETURN),
    SimpleOperation(Opcode.OP_ICONST_0),
    SimpleOperation(Opcode.OP_IRETURN),
  )
)
