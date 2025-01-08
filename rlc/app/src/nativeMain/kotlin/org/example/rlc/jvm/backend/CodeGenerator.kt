package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ClassFile

class CodeGenerator(private val buildOutputDir: String) {
  fun compileToJar(classes: List<ClassFile>, jarName: String) {
    val outputJar = when (buildOutputDir == "") {
      true -> jarName
      false -> "$buildOutputDir/$jarName"
    }

    createJarFile(compile(classes), outputJar)
  }

  fun compileToPlainFiles(classes: List<ClassFile>) {
    val compiledClasses = compile(classes)
    compiledClasses.forEach { cc -> createClassFile(cc, cc.fileName) }
  }

  private fun compile(classes: List<ClassFile>): List<CompiledBinaryFile> {
    val compiledClasses = mutableListOf<CompiledBinaryFile>()

    classes.forEach { classFile ->
      // Finally actual code generation is where it belongs.
      compiledClasses.add(ClassCompiler().compileClass(classFile))
    }

    return compiledClasses
  }
}
