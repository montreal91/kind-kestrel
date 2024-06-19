package org.example.rlc.jvm.ir

import kotlin.experimental.or

class MethodInfo(
  val methodName: Utf8Value,
  val methodDescriptor: Utf8Value,
  private val accessFlagList: List<MethodAccessFlags>,
  val attributeList: List<AttributeInfo>
) {
  val accessFlags: Short
    get() {
      var res: Short = 0
      for (flag in accessFlagList) {
        res = res or flag.value
      }
      return res
    }
}
