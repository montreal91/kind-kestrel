package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
import org.example.rlc.jvm.ir.MethodAccessFlags
import org.example.rlc.jvm.ir.MethodInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.Opcode
import org.example.rlc.jvm.ir.Operation
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
internal val loxClass: ClassFile
  get() {
    return ClassFile(
      thisClassInfo = "LoxClass".toClassInfo(),
      superClassInfo = javaLangObjectClassInfo,
      accessFlagList = listOf(ClassAccessFlags.SUPER),
      attributeList = listOf(),
      fieldList = listOf(finalStringName),
      methodList = listOf(generateLoxClassConstructor()),
      interfaceList = listOf()
    )
  }

private val nameRef = FieldRefInfo(
  label = "LoxClass.name:Ljava/lang/String",
  classInfo = "LoxClass".toClassInfo(),
  nameAndType = NameAndTypeInfo(
    label = "name:Ljava/lang/String",
    name = "name".toUtf8Value(),
    descriptor = javaStringDescriptor,
  ),
)

private val finalStringName: FieldInfo
  get() {
    return FieldInfo(
      accessFlagList = listOf(FieldAccessFlags.FINAL),
      fieldName = "name".toUtf8Value(),
      fieldDescriptor = javaStringDescriptor
    )
  }

private fun generateLoxClassConstructor(): MethodInfo {
  return MethodInfo(
    methodName = constructorMethodName,
    methodDescriptor = "(Ljava/lang/String;)V".toUtf8Value(),
    maxStack = 1,
    maxLocals = 1,
    accessFlagList = listOf(MethodAccessFlags.NONE),
    code = listOf(
      Operation(Opcode.OP_ALOAD_0),
      Operation(Opcode.OP_INVOKE_SPECIAL, listOf(objectConstructor)),
      Operation(Opcode.OP_ALOAD_0),
      Operation(Opcode.OP_ALOAD_1),
      Operation(Opcode.OP_PUTFIELD, listOf(nameRef)),
      Operation(Opcode.OP_RETURN)
    )
  )
}
