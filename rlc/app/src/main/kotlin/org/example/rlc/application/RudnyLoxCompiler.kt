package org.example.rlc.application

import org.example.rlc.frontend.Parser
import org.example.rlc.frontend.Scanner
import org.example.rlc.jvm.backend.CodeGenerator
import org.example.rlc.jvm.middleware.AstToClassFileIrConverter
import java.io.File
import kotlin.system.exitProcess

private fun readFile(pathName: String): String {
  val file = File(pathName)

  if (!file.exists()) {
    System.err.println("Could not open file $pathName.")
    exitProcess(status = 74)
  }

  return file.readText()
}

private fun compile(pathName: String) {
  val programText = readFile(pathName)
  val scanner = Scanner(programText)
  val parser = Parser(scanner.scan())
  val ast = parser.parse()

  if (scanner.hasErrors or parser.hasErrors) {
    for (error in scanner.getErrors()) {
      System.err.println(error)
    }

    for (error in parser.getErrors()) {
      System.err.println(error)
    }

    exitProcess(status = 65)
  }

  val classes = AstToClassFileIrConverter(pathName).convert(ast)
  CodeGenerator(buildOutputDir = ".").compileAll(classes)
}

fun main(args: Array<String>) {
  if (args.size != 1) {
    System.err.println("Usage: rlc path")
    exitProcess(status = 64)
  }

  compile(args[0])

  exitProcess(status = 0)
}
