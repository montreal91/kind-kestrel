package org.example.rlc.jvm.backend

import org.example.rlc.jvm.ir.ClassFile

/**
 * CodeGenerator is responsible for compiling a given list of classes
 * and outputting the result either as a JAR file or as individual class files.
 * It serves as a utility to manage the code generation and packaging process in a build system.
 *
 * @property buildOutputDir The directory where the generated output files will be stored.
 */
class CodeGenerator(private val buildOutputDir: String) {
  /**
   * Compiles a list of class files and packages them into a JAR file with the specified name.
   * The output JAR file is stored in the configured build output directory or in the current
   * directory if no output directory is specified.
   *
   * @param classes A list of `ClassFile` objects representing the class files to be compiled.
   * @param jarName The name of the JAR file to be generated. If no build output directory is
   *                set, the JAR is created in the current directory; otherwise, it is stored
   *                in the configured output directory.
   */
  fun compileToJar(classes: List<ClassFile>, jarName: String) {
    val outputJar = when (buildOutputDir == "") {
      true -> jarName
      false -> "$buildOutputDir/$jarName"
    }

    createJarFile(compile(classes), outputJar)
  }

  /**
   * Compiles a list of class files into plain `.class` files and stores them
   * in the output location specified by their respective file names.
   *
   * @param classes A list of `ClassFile` objects representing the class files to be compiled.
   * Each `ClassFile` contains information about the class, its methods, fields, interfaces,
   * and attributes necessary for compilation.
   */
  fun compileToPlainFiles(classes: List<ClassFile>) {
    val compiledClasses = compile(classes)
    compiledClasses.forEach { cc -> createClassFile(cc, cc.fileName) }
  }

  private fun compile(classes: List<ClassFile>): List<CompiledBinaryFile> {
    val compiledClasses = mutableListOf<CompiledBinaryFile>()

    classes.forEach { classFile ->
      // Finally, actual code generation is where it belongs.
      compiledClasses.add(ClassCompiler().compileClass(classFile))
    }

    return compiledClasses
  }
}
