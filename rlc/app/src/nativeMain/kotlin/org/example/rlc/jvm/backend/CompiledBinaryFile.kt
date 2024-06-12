package org.example.rlc.jvm.backend

data class CompiledBinaryFile(
  val fileName: String,
  val binary: List<Byte>,
)
