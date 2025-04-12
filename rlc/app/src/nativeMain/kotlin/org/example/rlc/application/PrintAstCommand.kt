package org.example.rlc.application

import kotlinx.cinterop.ExperimentalForeignApi
import org.example.rlc.frontend.Parser
import org.example.rlc.frontend.Scanner
import org.example.rlc.jvm.middleware.AstPrettyPrinter
import platform.posix.exit
import platform.posix.fprintf
import platform.posix.stderr
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalForeignApi::class, ExperimentalUuidApi::class)
internal fun prettyPrintAst(pathName: String) {
  val programText = readFile(pathName)
  val scanner = Scanner(programText)
  val parser = Parser(scanner.scan())
  val ast = parser.parse()

  if (scanner.hasErrors or parser.hasErrors) {
    for (error in scanner.getErrors()) {
      fprintf(__stream = stderr, __format = "$error\n")
    }

    for (error in parser.getErrors()) {
      fprintf(__stream = stderr, __format = "$error\n")
    }
    exit(_Code = 65)
  }

  val astConverter = AstPrettyPrinter()
  val res = astConverter.print(ast)

  println(res)
}
