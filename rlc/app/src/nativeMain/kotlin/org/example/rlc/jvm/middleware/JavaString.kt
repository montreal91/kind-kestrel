package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.ObjectVti

object JavaString {
  internal val javaLangStringClassInfo = ClassInfo(className = "java/lang/String")
  internal val VERIFICATION_TYPE = ObjectVti(javaLangStringClassInfo)
}
