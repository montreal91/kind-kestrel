package org.example.rlc.frontend

import org.example.rlc.frontend.ast.ExprStmt
import org.example.rlc.frontend.ast.PrintStmt
import kotlin.test.Test
import kotlin.test.assertEquals


class ParserTest {

  @Test
  fun parse() {
    val text = this::class.java.classLoader.getResource("parser_one.lox")!!.readText()
    val scanner = Scanner(text)
    val actual = Parser(tokens = scanner.scan()).parse()
    for (stmt in actual) {
      when (stmt) {
        is PrintStmt -> printExpr(stmt.expr)
        is ExprStmt -> printExpr(stmt.expr)
      }
    }
    assertEquals(actual, actual)
  }
}
