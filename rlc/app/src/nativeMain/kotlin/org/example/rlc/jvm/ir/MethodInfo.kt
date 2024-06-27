package org.example.rlc.jvm.ir

import kotlin.experimental.or

class MethodInfo(
  val methodName: Utf8Value,
  val methodDescriptor: Utf8Value,
  private val accessFlagList: List<MethodAccessFlags>,
  val attributeList: List<AttributeInfo>,
  val signature: MethodSignature,
  val isStatic: Boolean,
) {

  val newDescriptor: Utf8Value
    get() {
      val res = StringBuilder()
      res.append("(")


      res.append(signature.returnType)

      return Utf8Value(res.toString())
    }

  constructor(
    methodName: String,
    methodDescriptor: String,
    accessFlagList: List<MethodAccessFlags>,
    attributeList: List<AttributeInfo>,
    signature: MethodSignature,
    isStatic: Boolean
  ) : this(
    methodName.toUtf8Value(),
    methodDescriptor.toUtf8Value(),
    accessFlagList,
    attributeList,
    signature,
    isStatic,
  )

  val accessFlags: Short
    get() {
      var res: Short = 0
      for (flag in accessFlagList) {
        res = res or flag.value
      }
      return res
    }

  private fun makeDescriptorValueFromSignature(value: VerificationTypeInfo): String {
    val res = StringBuilder()

    when (value) {
      is TopVti -> {}
      is DoubleVti -> res.append("D")
      is IntegerVti -> res.append("I")
      is ObjectVti -> res.append("L;")
    }

    return res.toString()
  }

}
