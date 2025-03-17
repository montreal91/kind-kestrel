package org.example.rlc.jvm.ir

import kotlin.experimental.or

class MethodInfo(
  val methodName: Utf8Value,
  private val accessFlagList: List<MethodAccessFlags>,
  val attributeList: List<AttributeInfo>,
  val signature: MethodSignature,
  val isStatic: Boolean,
  val localVariables: List<VerificationTypeInfo>,
) {
  // Thought for the future:
  // Theoretically we can put here information about locals.
  // Philosophical question is, where information about local variables should be stored.
  // Does it belong to the code, or does it belong to the method.
  // And how do I answer this question?
  // ***
  // And now I have a feeling that JVM bytecode mixes up this stuff a bit.

  val methodDescriptor: Utf8Value
    get() {
      val res = StringBuilder()
      res.append("(")

      for (arg in signature.arguments) {
        res.append(makeDescriptorValueFromSignature(arg))
      }

      res.append(")")
      res.append(makeDescriptorValueFromSignature(signature.returnType))

      val resStr = res.toString()
      println("            Calculated method ref: $resStr")
      return Utf8Value(resStr)
    }

  constructor(
    methodName: String,
    accessFlagList: List<MethodAccessFlags>,
    attributeList: List<AttributeInfo>,
    signature: MethodSignature,
    isStatic: Boolean
  ) : this(
    methodName.toUtf8Value(),
    accessFlagList,
    attributeList,
    signature,
    isStatic,
    listOf(),
  )

  constructor(
    methodName: String,
    accessFlagList: List<MethodAccessFlags>,
    attributeList: List<AttributeInfo>,
    signature: MethodSignature,
    isStatic: Boolean,
    localVariables: List<VerificationTypeInfo>
  ) : this(
    methodName.toUtf8Value(),
    accessFlagList,
    attributeList,
    signature,
    isStatic,
    localVariables,
  )

  val isAbstract: Boolean = accessFlagList.contains(MethodAccessFlags.ABSTRACT)

  val accessFlags: Short
    get() {
      var res: Short = 0
      for (flag in accessFlagList) {
        res = res or flag.value
      }
      return res
    }

  val signatureConstants: List<ConstantPoolInfo>
    get() {
      val res = mutableListOf<ConstantPoolInfo>()
      for (s in signature.arguments) {
        res.add(vtiToCpi(s))
      }
      res.add(vtiToCpi(signature.returnType))
      return res
    }

  private fun makeDescriptorValueFromSignature(value: VerificationTypeInfo): String {
    val res = StringBuilder()

    when (value) {
      is TopVti -> {}
      is DoubleVti -> res.append("D")
      is IntegerVti -> res.append("I")
      is ObjectVti -> res.append(objectVtiToDescriptor(value))
      is EmptyVti -> res.append("V")
      is BooleanVti -> res.append("Z")
      is NullVariableVti -> {}
      is LongVti -> res.append("J")
    }

    return res.toString()
  }

  private fun vtiToCpi(value: VerificationTypeInfo): ConstantPoolInfo = when(value) {
    is ObjectVti -> objectVtiToClassInfo(value)
    is BooleanVti -> "Z".toUtf8Value()
    is DoubleVti -> "D".toUtf8Value()
    is EmptyVti -> "".toUtf8Value()
    is IntegerVti -> "I".toUtf8Value()
    is NullVariableVti -> "".toUtf8Value()
    is TopVti -> "".toUtf8Value()
    is LongVti -> "J".toUtf8Value()
  }

  private fun objectVtiToClassInfo(objectVti: ObjectVti): ClassInfo {
    return objectVti.classInfo
  }

  private fun objectVtiToDescriptor(vti: ObjectVti): String {
    val res = StringBuilder()

    if (vti.isArray) {
      return vti.classInfo.className.encodedString
    }

    res.append("L")
    res.append(vti.classInfo.className.encodedString)
    res.append(";")
    return res.toString()
  }
}
