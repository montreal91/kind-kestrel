package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassAccessFlags
import org.example.rlc.jvm.ir.ClassFile
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.CodeAttribute
import org.example.rlc.jvm.ir.DoubleVti
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.FieldAccessFlags
import org.example.rlc.jvm.ir.FieldInfo
import org.example.rlc.jvm.ir.FieldRefInfo
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
internal fun loxDoubleCf() = ClassFile(
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
    addEqMethod(),
    greater(),
    greaterOrEquals(),
    less(),
    lessOrEquals(),
    truthy(),
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
    ShortConstantOperation(Opcode.OP_GETSTATIC, loxDoubleClass, ObjectVti(loxDoubleClassInfo, isArray = false)),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxObjectConstructor),
    SimpleOperation(Opcode.OP_ALOAD_0),
    SimpleOperation(Opcode.OP_DLOAD_1),
    ShortConstantOperation(Opcode.OP_PUTFIELD, doubleValueFieldRefInfo),
    SimpleOperation(Opcode.OP_RETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return MethodInfo(
    methodName = constructorMethodName,
    accessFlagList = listOf(),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(DoubleVti()), EmptyVti())
  )
}

private fun addArithmeticMethodInfo(methodName: String, operation: Opcode): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(operation),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return MethodInfo(
    methodName = methodName,
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(ObjectVti(loxDoubleClassInfo)), ObjectVti(loxDoubleClassInfo))
  )
}

private fun addUnaryMethodInfo(methodName: String, operation: Opcode): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxDoubleClassInfo, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(operation),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxDoubleConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return MethodInfo(
    methodName = methodName,
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(), ObjectVti(loxDoubleClassInfo))
  )
}

private fun addEqMethod(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxDoubleClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_CHECKCAST, loxDoubleClassInfo),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_DCMPG),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_IXOR),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN),
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)

  return MethodInfo(
    methodName = "__eq__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = loxBinaryOpSignature
  )
}

private fun truthy() = alwaysTruthy()

private fun toString(): MethodInfo {
  val doubleToStringMethodRef = MethodRefInfo(
    label = "java/lang/Double.toString:(D)Ljava/lang/String;",
    classInfo = ClassInfo(className = "java/lang/Double"),
    nameAndType = NameAndTypeInfo(
      label = "toString:(D)Ljava/lang/String;",
      name = "toString".toUtf8Value(),
      descriptor = "(D)Ljava/lang/String;".toUtf8Value(),
    ),
    argsSize = 3,
    returnSize = 1,
    returnTypeInfo = JavaString.VERIFICATION_TYPE
  )

  val code = listOf(
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    ShortConstantOperation(Opcode.OP_INVOKE_STATIC, doubleToStringMethodRef),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 1, code = code)

  return MethodInfo(
    methodName = "toString",
    accessFlagList = listOf(MethodAccessFlags.PUBLIC),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = MethodSignature(listOf(), JavaString.VERIFICATION_TYPE)
  )
}

private fun greater(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_DCMPG),
    SimpleOperation(Opcode.OP_ICONST_3),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_IADD),
    SimpleOperation(Opcode.OP_ICONST_3),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_ISHR),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)
  return MethodInfo(
    methodName = "__gt__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = loxNumberComparisonSignature
  )
}

private fun greaterOrEquals(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_DCMPG),
    SimpleOperation(Opcode.OP_ICONST_3),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_2),
    SimpleOperation(Opcode.OP_IXOR),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_ISHR),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)
  return MethodInfo(
    methodName = "__ge__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = loxNumberComparisonSignature
  )
}

private fun less(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_DCMPG),
    SimpleOperation(Opcode.OP_ICONST_3),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_ISHR),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)
  return MethodInfo(
    methodName = "__lt__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = loxNumberComparisonSignature
  )
}

private fun lessOrEquals(): MethodInfo {
  val code = listOf(
    ShortConstantOperation(Opcode.OP_NEW, loxBooleanClassInfo, ObjectVti(loxBooleanClassInfo)),
    SimpleOperation(Opcode.OP_DUP),
    SimpleOperation(Opcode.OP_ALOAD_0),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_ALOAD_1),
    ShortConstantOperation(Opcode.OP_GETFIELD, doubleValueFieldRefInfo, DoubleVti()),
    SimpleOperation(Opcode.OP_DCMPG),
    SimpleOperation(Opcode.OP_ICONST_3),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_IADD),
    SimpleOperation(Opcode.OP_ICONST_3),
    SimpleOperation(Opcode.OP_IAND),
    SimpleOperation(Opcode.OP_ICONST_2),
    SimpleOperation(Opcode.OP_IXOR),
    SimpleOperation(Opcode.OP_ICONST_1),
    SimpleOperation(Opcode.OP_ISHR),
    ShortConstantOperation(Opcode.OP_INVOKE_SPECIAL, loxBooleanConstructorInfo),
    SimpleOperation(Opcode.OP_ARETURN)
  )

  val codeAttribute = CodeAttribute(argsSize = 2, code = code)
  return MethodInfo(
    methodName = "__le__",
    accessFlagList = listOf(MethodAccessFlags.NONE),
    attributeList = listOf(codeAttribute),
    isStatic = true,
    signature = loxNumberComparisonSignature
  )
}
