package org.example.rlc.application

import org.example.rlc.frontend.Scanner
import java.io.File
import kotlin.system.exitProcess

fun readFile(pathName: String): String {
  val file = File(pathName)

  if (!file.exists()) {
    System.err.println("Could not open file $pathName.")
    exitProcess(status = 74)
  }

  return file.readText()
}

fun compile(pathName: String) {
  val programText = readFile(pathName)
  val scanner = Scanner(programText)

  val tokens = scanner.scan()

  for (token in tokens) {
    println(token)
  }
  if (scanner.hasErrors) {
    exitProcess(status = 65)
  }
}

fun main(args: Array<String>) {
  if (args.size != 1) {
    System.err.println("Usage: rlc path")
    exitProcess(status = 64)
  }

  compile(args[0])

  exitProcess(status = 0)
}
