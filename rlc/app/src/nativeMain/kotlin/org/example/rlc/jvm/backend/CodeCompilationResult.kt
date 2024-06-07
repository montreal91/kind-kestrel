package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.StackMapTableAttribute

data class CodeCompilationResult(
  val bytes: List<Byte>,
  val stackMapTableAttribute: StackMapTableAttribute
)
