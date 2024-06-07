package org.example.rlc.frontend

import org.example.rlc.application.readFile
import kotlin.test.Test
import kotlin.test.assertEquals


internal data class PositiveTestCase(
  val fileName: String,
  val expected: String
)

internal data class NegativeTestCase(
  val fileName: String,
  val expectedErrors: List<String>
)

private fun Parser.getErrorMessages(): List<String> {
  val messages = mutableListOf<String>()
  for (error in this.getErrors()) {
    messages.add(error.toString())
  }

  return messages
}


class ParserTest {
  private val positive = listOf(
    PositiveTestCase(
      fileName = "parser_one.lox",
      expected = "(expr (+ 1 2))",
    ),
    PositiveTestCase(
      fileName = "parser_two.lox",
      expected = "(print (- (+ (- 1) (* 2 3)) (/ 4 5)))",
    ),
    PositiveTestCase(
      fileName = "parser_or.lox",
      expected = "(expr (or true false))",
    ),
    PositiveTestCase(
      fileName = "parser_and.lox",
      expected = "(expr (and false true))",
    ),
    PositiveTestCase(
      fileName = "parser_grouping.lox",
      expected = "(expr (* (group (+ 1 2)) (group (- (- 3) 4))))",
    ),
  )

  private val negative = listOf(
    NegativeTestCase(
      fileName = "parser_bad_leading_dot.lox",
      expectedErrors = listOf("[line 2] Error at '.': Expect expression.")
    ),
    NegativeTestCase(
      fileName = "parser_bad_print.lox",
      expectedErrors = listOf("[line 2] Error at ';': Expect expression.")
    ),
    // TODO: explore this error case;
    // Frankly speaking, if this compiler passes lox testsuite, then I don't care.
    // But for my own language, I would like to achieve 100% branch test coverage.
    NegativeTestCase(
      fileName = "parser_bad_or.lox",
      expectedErrors = listOf("[line 2] Error at end. : Expect expression.")
    )
  )

  @Test
  fun positiveCases() {
    positive.forEach { case ->
      val text = readFile(pathName = "${PATH_PREFIX}\\${case.fileName}")
      val scanner = Scanner(text)
      val parser = Parser(tokens = scanner.scan())
      val ast = parser.parse()
      assertEquals(expected = false, actual = parser.hasErrors)
      assertEquals(case.expected, AstToLispExprConverter(ast).convertToLisp())
    }
  }

  @Test
  fun negativeCases() {
    negative.forEach { case ->
      val text = readFile(pathName = "${PATH_PREFIX}/${case.fileName}")
      val scanner = Scanner(text)
      val parser = Parser(tokens = scanner.scan())
      parser.parse()
      assertEquals(expected = true, actual = parser.hasErrors)
      assertEquals(expected = case.expectedErrors, actual = parser.getErrorMessages())
    }
  }
}
