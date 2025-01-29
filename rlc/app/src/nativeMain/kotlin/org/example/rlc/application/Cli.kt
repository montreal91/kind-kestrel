package org.example.rlc.application

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import org.example.rlc.frontend.Parser
import org.example.rlc.frontend.Resolver
import org.example.rlc.frontend.Scanner
import org.example.rlc.jvm.backend.CodeGenerator
import org.example.rlc.jvm.middleware.AstToClassFileIrConverter
import org.example.rlc.jvm.middleware.StackMapTableMaker
import platform.posix.F_OK
import platform.posix.access
import platform.posix.fopen
import platform.posix.fgets
import platform.posix.fclose
import platform.posix.fprintf
import platform.posix.stderr
import platform.posix.exit


internal class Cli : CliktCommand() {
  private val path: String by argument(help = "Path to the file.")
  private val debugMode: Boolean by option(
    names = arrayOf("--debug"),
    metavar = "-d",
    help="Enable debug stuff."
  ).flag()

  private val printAst: Boolean by option(
    names = arrayOf("--print-ast"),
    metavar = "-p",
    help = "Pretty-print ast of the program."
  ).flag()

  override fun run() {
    if (printAst) {
      prettyPrintAst(path)
      return
    }

    compile(path, debugMode)
  }
}

@OptIn(ExperimentalForeignApi::class)
internal fun readFile(pathName: String): String {
  try {
    if (access(pathName, F_OK) != 0) {
      throw RuntimeException()
    }

    val file = fopen(pathName, _Mode = "r")
    val buffer = StringBuilder()
    val lineBuffer = ByteArray(size = 4096)

    while (true) {
      val line = fgets(lineBuffer.refTo(index = 0), lineBuffer.size, file) ?: break
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
private fun compile(pathName: String, debugMode: Boolean) {
  val programText = readFile(pathName)
  val scanner = Scanner(programText)
  val parser = Parser(scanner.scan())
  val ast = parser.parse()

  val resolver = Resolver()
  val resolutionTable = resolver.resolve(ast)

  if (scanner.hasErrors or parser.hasErrors) {
    for (error in scanner.getErrors()) {
      fprintf(__stream = stderr, __format = error.toString())
    }

    for (error in parser.getErrors()) {
      fprintf(__stream = stderr, __format = error.toString())
    }
    exit(_Code = 65)
  }

  if (resolver.hasErrors) {
    for (error in resolver.getErrors()) {
      fprintf(__stream = stderr, __format = error.toString())
    }
  }

  val astConverter = AstToClassFileIrConverter(resolutionTable)
  val classes = astConverter.convert(ast)
  val stackMapTableMaker = StackMapTableMaker()
  stackMapTableMaker.fillStackMapTables(classes)

  if (debugMode) {
    CodeGenerator(buildOutputDir = ".").compileToPlainFiles(classes)
  }
  else {
    CodeGenerator(buildOutputDir = ".").compileToJar(
      classes = classes,
      jarName = outputFileName(pathName)
    )
  }
}

private fun outputFileName(inputFileName: String): String {
  val loxFileName = inputFileName.substringAfterLast(delimiter = "/")
  return loxFileName.substringBeforeLast(delimiter = ".") + ".jar"
}
