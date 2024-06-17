package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.CodeAttribute
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
import org.example.rlc.jvm.ir.toClassInfo
import org.example.rlc.jvm.ir.toUtf8Value


/***
 * The intermediate representation that corresponds the following Java code
 *
 * ```
 * class LoxDouble extends LoxObject {
 *   final double value;
 *   LoxDouble(double value) {
 *     super(LoxObject.loxDoubleClass);
 *     this.value = value;
 *   }
 *
 *   LoxDouble __add__(LoxDouble other) {
 *     return new LoxDouble(value + other.value);
 *   }
 *
 *   LoxDouble __sub__(LoxDouble other) {
 *     return new LoxDouble(value - other.value);
 *   }
 *
 *   LoxDouble __mul__(LoxDouble other) {
 *     return new LoxDouble(value * other.value);
 *   }
 *
 *   LoxDouble __div__(LoxDouble other) {
 *     return new LoxDouble(value / other.value);
 *   }
 *
 *   LoxDouble __neg__() {
 *     return new LoxDouble(- value);
 *   }
 *
 *   @Override
 *   public String toString() {
 *     return Double.toString(this.value);
 *   }
 * }
 * ```
 */
internal fun loxDouble() = ClassFile(
  thisClassInfo = loxDoubleClassInfo,
  superClassInfo = loxObjectClassInfo,
  accessFlagList = listOf(ClassAccessFlags.SUPER),
  attributeList = listOf(),
  fieldList = listOf(valueFieldInfo()),
  methodList = listOf(
    loxDoubleConstructor(),
    addArithmeticMethodInfo(methodName = "__add__", operation = Opcode.OP_DADD),
    addArithmeticMethodInfo(methodName = "__sub__", operation = Opcode.OP_DSUB),
    addArithmeticMethodInfo(methodName = "__mul__", operation = Opcode.OP_DMUL),
    addArithmeticMethodInfo(methodName = "__div__", operation = Opcode.OP_DDIV),
    addUnaryMethodInfo(methodName = "__neg__", operation = Opcode.OP_DNEG),
    toString()
  ),
  interfaceList = listOf(),
)

private fun valueFieldInfo() = FieldInfo(
  accessFlagList = listOf(FieldAccessFlags.FINAL),
  fieldName = "value".toUtf8Value(),
  fieldDescriptor = "D".toUtf8Value(),
)

private fun loxDoubleConstructor(): MethodInfo {
  val loxDoubleClass = FieldRefInfo(
    label = "LoxObject.LOX_DOUBLE_CLASS:LLoxClass;",
    classInfo = loxDoubleClassInfo,
    nameAndType = NameAndTypeInfo(
      label = "LOX_DOUBLE_CLASS:LLoxClass;",
      name = "LOX_DOUBLE_CLASS".toUtf8Value(),
      descriptor = loxClassDescriptor,
    )
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETSTATIC, loxDoubleClass),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_DLOAD_1),
    ShortConstantOperation(Opcode.OP_PUTFIELD, doubleValueFieldRefInfo),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "<init>".toUtf8Value(),
    methodDescriptor = "(D)V".toUtf8Value(),
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
  )
}

private fun addArithmeticMethodInfo(methodName: String, operation: Opcode): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo),
    SimpleOperation(operation),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = methodName.toUtf8Value(),
    methodDescriptor = "(LLoxDouble;)LLoxDouble;".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
  )
}

private fun addUnaryMethodInfo(methodName: String, operation: Opcode): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo),
    SimpleOperation(operation),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 2,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = methodName.toUtf8Value(),
    methodDescriptor = "()LLoxDouble;".toUtf8Value(),
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
  )
}

private fun toString(): MethodInfo {
  val doubleToStringMethodRef = MethodRefInfo(
    label = "java/lang/Double.toString:(D)Ljava/lang/String;",
    classInfo = "java/lang/Double".toClassInfo(),
    nameAndType = NameAndTypeInfo(
      label = "toString:(D)Ljava/lang/String;",
      name = "toString".toUtf8Value(),
      descriptor = "(D)Ljava/lang/String;".toUtf8Value(),
    ),
    argsSize = 3,
    returnSize = 1
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo),
    ShortConstantOperation(Opcode.OP_INVOKE_STATIC, doubleToStringMethodRef),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(
    argsSize = 1,
    code = code,
    exceptionTable = 0,
    attributes = listOf()
  )

  return MethodInfo(
    methodName = "toString".toUtf8Value(),
    methodDescriptor = toStringDescriptor,
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(codeAttribute)
  )
}
