package org.example.rlc.jvm.ir

import kotlin.experimental.or

class MethodInfo(
  val methodName: Utf8Value,
  val methodDescriptor: Utf8Value,
  val maxStack: Short,
  val maxLocals: Short,
  private val accessFlagList: List<MethodAccessFlags>,
  val code: List<Operation>
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
