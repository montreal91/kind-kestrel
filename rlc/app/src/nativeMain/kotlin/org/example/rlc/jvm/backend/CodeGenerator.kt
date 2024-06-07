package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ClassFile

class CodeGenerator(private val buildOutputDir: String) {
  fun compileAll(classes: List<ClassFile>) {
    classes.forEach(this::classToFile)
  }

  private fun classToFile(classFile: ClassFile) {
    val bytecode = ClassCompiler().compileClass(classFile)

    val filepath = when (buildOutputDir == "") {
      true -> classFile.thisClassInfo.className.label
      false -> buildOutputDir + "/" + classFile.thisClassInfo.className.label
    }

    bytecode.writeToFile(filepath)
  }
}
