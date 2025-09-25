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
import kotlin.uuid.ExperimentalUuidApi


/**
 * Represents a CLI (Command Line Interface) application that processes commands and arguments for
 * a compiler program. This class extends CliktCommand to handle user interaction via the command line.
 *
 * The available parameters include:
 * - A required file path to process.
 * - Optional flags for additional functionality like enabling debug mode or printing the program's AST.
 *
 * Parameters:
 * - `path`: The path to the file to be processed, provided as a required command-line argument.
 * - `debugMode`: An optional flag (`--debug`), which enables additional debug-related tasks during compilation.
 * - `printAst`: An optional flag (`--print-ast`), which triggers pretty-printing the abstract syntax tree (AST)
 *               of the program instead of compilation.
 *
 * Behavior:
 * - If the `--print-ast` flag is supplied, the program parses the file to an
 *   AST and outputs its human-readable representation.
 *   This is achieved using the `prettyPrintAst` function.
 * - If no flags are supplied, the program compiles the file into JAR or raw classes,
 *   depending on the presence of the `--debug` flag.
 *   The compilation process is handled internally by the `compile` function.
 */
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

  /**
   * Executes the primary logic of the CLI application based on the provided command-line arguments.
   *
   * Behavior:
   * - If the `--print-ast` flag is set, it calls the `prettyPrintAst` function to parse the file
   *   and output its abstract syntax tree (AST) in a human-readable format.
   * - Otherwise, it invokes the `compile` function to process the file, which can generate either plain class
   *   files or a JAR file depending on whether the `--debug` flag is enabled.
   *
   * This function acts as an entry point to determine the application's runtime behavior.
   */
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

/**
 * Compiles a source file into an executable format, handling lexical analysis, parsing,
 * semantic analysis, and code generation. Depending on the provided debug mode,
 * compilation outputs could include plain files or a JAR file.
 *
 * @param pathName The path to the source file to be compiled.
 * @param debugMode A flag indicating whether to compile in debug mode.
 *        If true, the output will be plain class files. If false, the output will be a JAR file.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalUuidApi::class)
private fun compile(pathName: String, debugMode: Boolean) {
  val programText = readFile(pathName)
  val scanner = Scanner(programText)
  val parser = Parser(scanner.scan())
  val ast = parser.parse()

  val resolver = Resolver()
  val resolutionTable = resolver.resolve(ast)

  if (scanner.hasErrors or parser.hasErrors) {
    for (error in scanner.getErrors()) {
      fprintf(__stream = stderr, __format = "$error\n")
    }

    for (error in parser.getErrors()) {
      fprintf(__stream = stderr, __format = "$error\n")
    }
    exit(_Code = 65)
  }

  if (resolver.hasErrors) {
    for (error in resolver.getErrors()) {
      fprintf(__stream = stderr, __format = "$error\n")
    }
    exit(_Code = 65)
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
