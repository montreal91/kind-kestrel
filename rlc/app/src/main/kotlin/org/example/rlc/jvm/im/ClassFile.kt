package org.example.rlc.jvm.im

import kotlin.experimental.or

class ClassFile(
  val thisClassInfo: ClassInfo,
  val superClassInfo: ClassInfo,
  val accessFlagList: List<ClassAccessFlags>,
  val interfaceList: List<InterfaceInfo>,
  val fieldList: List<FieldInfo>,
  val methodList: List<MethodInfo>,
  val attributeList: List<AttributeInfo>,
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
