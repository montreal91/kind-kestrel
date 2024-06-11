package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ClassFile

class CodeGenerator(private val buildOutputDir: String) {
  fun compileToJar(classes: List<ClassFile>, jarName: String) {
    val filepath = when (buildOutputDir == "") {
      true -> jarName
      false -> "$buildOutputDir/$jarName"
    }
    createJarFile(classes, filepath)
  }
}
