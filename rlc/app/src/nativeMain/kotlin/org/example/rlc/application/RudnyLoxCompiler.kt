@file:OptIn(ExperimentalForeignApi::class)

package org.example.rlc.application

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import org.example.rlc.frontend.Parser
import org.example.rlc.frontend.Scanner
import org.example.rlc.jvm.backend.CodeGenerator
import org.example.rlc.jvm.middleware.AstToClassFileIrConverter
import platform.posix.F_OK
import platform.posix.access
import platform.posix.exit
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fprintf
import platform.posix.stderr


@OptIn(ExperimentalForeignApi::class)
internal fun readFile(pathName: String): String {
  try {
    if (access(pathName, F_OK) != 0) {
      throw RuntimeException()
    }

    val file = fopen(pathName, _Mode = "r")
    val buffer = StringBuilder()
    val lineBuffer = ByteArray(4096)

    while (true) {
      val line = fgets(lineBuffer.refTo(0), lineBuffer.size, file) ?: break
      buffer.append(line.toKString())
    }

    fclose(file)
    return buffer.toString()
  } catch (e: Exception) {
    fprintf(stderr, __format = "Could not open file $pathName.")
    exit(_Code = 74)
  }

  exit(_Code = 74)
  throw RuntimeException("This should never happen.")
}

@OptIn(ExperimentalForeignApi::class)
private fun compile(pathName: String) {
  val programText = readFile(pathName)
  val scanner = Scanner(programText)
  val parser = Parser(scanner.scan())
  val ast = parser.parse()

  if (scanner.hasErrors or parser.hasErrors) {
    for (error in scanner.getErrors()) {
      fprintf(__stream = stderr, __format=error.toString())
    }

    for (error in parser.getErrors()) {
      fprintf(__stream = stderr, __format=error.toString())
    }
    exit(_Code = 65)
  }

  val classes = AstToClassFileIrConverter(pathName).convert(ast)
  CodeGenerator(buildOutputDir = ".").compileAll(classes)
}

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
  if (args.size != 1) {
    fprintf(stderr, __format = "Usage: rlc [path]")
    exit(_Code = 64)
  }

  compile(args[0])
  exit(_Code = 0)
}
