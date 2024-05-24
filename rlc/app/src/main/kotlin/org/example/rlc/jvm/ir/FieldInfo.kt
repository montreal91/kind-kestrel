package org.example.rlc.jvm.ir

import kotlin.experimental.or

class FieldInfo(
  private val accessFlagList: List<FieldAccessFlags>,
  val fieldName: Utf8Value,
  val fieldDescriptor: Utf8Value
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
