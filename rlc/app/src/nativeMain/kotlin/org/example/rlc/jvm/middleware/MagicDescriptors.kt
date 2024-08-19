package org.example.rlc.jvm.middleware

import org.example.rlc.jvm.ir.MethodSignature
import org.example.rlc.jvm.ir.ObjectVti
import org.example.rlc.jvm.ir.toUtf8Value

internal val loxBinaryOpDescriptor = "(LLoxObject;)LLoxObject;".toUtf8Value()
internal val loxUnaryOpDescriptor = "()LLoxObject;".toUtf8Value()
internal val loxClassDescriptor = "LLoxClass;".toUtf8Value()
internal val toStringDescriptor = "()Ljava/lang/String;".toUtf8Value()
internal val numberComparisonDescriptor = "(LLoxDouble;)LLoxBoolean;".toUtf8Value()

internal val loxBinaryOpSignature = MethodSignature(listOf(ObjectVti(loxObjectClassInfo)), ObjectVti(loxObjectClassInfo))
internal val loxUnaryOpSignature = MethodSignature(listOf(), ObjectVti(loxObjectClassInfo))
internal val loxNumberComparisonSignature = MethodSignature(
  listOf(ObjectVti(loxDoubleClassInfo)),
  ObjectVti(loxBooleanClassInfo),
)
