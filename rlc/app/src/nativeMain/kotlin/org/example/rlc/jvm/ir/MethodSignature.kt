package org.example.rlc.jvm.ir

data class MethodSignature(
  val arguments: List<VerificationTypeInfo>,
  val returnType: VerificationTypeInfo
)
