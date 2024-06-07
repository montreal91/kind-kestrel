package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ClassFile

class CodeGenerator(private val buildOutputDir: String) {
  fun compileAll(classes: List<ClassFile>) {
    classes.forEach(this::classToFile)
  }

  fun compileToJar(classes: List<ClassFile>, jarName: String, mainClassName: String) {
    val filepath = when (buildOutputDir == "") {
      true -> jarName
      false -> "$buildOutputDir/$jarName"
    }
    createJarFile(classes, filepath, mainClassName)
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
