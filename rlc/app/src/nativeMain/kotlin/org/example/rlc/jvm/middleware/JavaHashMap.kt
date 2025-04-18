package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.BooleanVti
import org.example.rlc.jvm.ir.ClassInfo
import org.example.rlc.jvm.ir.EmptyVti
import org.example.rlc.jvm.ir.MethodRefInfo
import org.example.rlc.jvm.ir.NameAndTypeInfo
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.toUtf8Value

internal object JavaHashMap {
  internal val CLASS_INFO = ClassInfo(className = "java/util/HashMap")

  internal val VERIFICATION_TYPE = ObjectVti(classInfo = CLASS_INFO)

  internal val CONSTRUCTOR = MethodRefInfo(
    label = "java/util/HashMap.\"<init>\":()V",
    classInfo = CLASS_INFO,
    nameAndType = NameAndTypeInfo(
      label = "\"<init>\":()V",
      name = "<init>".toUtf8Value(),
      descriptor = "()V".toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 0,
    returnTypeInfo = EmptyVti()
  )

  internal val CONTAINS_KEY = MethodRefInfo(
    label = "java/lang/HashMap.containsKey:$containsKeySignature",
    classInfo = CLASS_INFO,
    nameAndType = NameAndTypeInfo(
      label = "containsKey:$containsKeySignature",
      name = "containsKey".toUtf8Value(),
      descriptor = containsKeySignature.toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = BooleanVti()
  )

  internal val GET = MethodRefInfo(
    label = "java/lang/HashMap.get:$mapGetSignature",
    classInfo = CLASS_INFO,
    nameAndType = NameAndTypeInfo(
      label = "get:$mapGetSignature",
      name = "get".toUtf8Value(),
      descriptor = mapGetSignature.toUtf8Value(),
    ),
    argsSize = 2,
    returnSize = 1,
    returnTypeInfo = BooleanVti()
  )

  internal val PUT = MethodRefInfo(
    label = "java/lang/HashMap.put:$putSignature",
    classInfo = CLASS_INFO,
    nameAndType = NameAndTypeInfo(
      label = "put:$putSignature",
      name = "put".toUtf8Value(),
      descriptor = putSignature.toUtf8Value(),
    ),
    argsSize = 1,
    returnSize = 1,
    returnTypeInfo = ObjectVti(loxObjectClassInfo, isArray = false)
  )
}

private const val mapGetSignature = "(Ljava/lang/Object;)Ljava/lang/Object;"
private const val putSignature = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
private const val containsKeySignature = "(Ljava/lang/Object;)Z"
