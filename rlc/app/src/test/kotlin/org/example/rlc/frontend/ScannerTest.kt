package org.example.rlc.frontend

import kotlin.test.Test
import kotlin.test.assertEquals


class ScannerTest {
  private val cases = listOf(
    Pair(
      "scanner_identifiers.lox",
      listOf(
        Token(Token.Type.IDENTIFIER, "andy", 1),
        Token(Token.Type.IDENTIFIER, "formless", 1),
        Token(Token.Type.IDENTIFIER, "fo", 1),
        Token(Token.Type.IDENTIFIER, "_", 1),
        Token(Token.Type.IDENTIFIER, "_123", 1),
        Token(Token.Type.IDENTIFIER, "_abc", 1),
        Token(Token.Type.IDENTIFIER, "ab123", 1),
        Token(Token.Type.IDENTIFIER, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_", 2),
        Token(Token.Type.EOF, "", 13),
      )
    ),
    Pair(
      "scanner_keywords.lox",
      listOf(
        Token(Token.Type.AND, "and", 1),
        Token(Token.Type.CLASS, "class", 1),
        Token(Token.Type.ELSE, "else", 1),
        Token(Token.Type.FALSE, "false", 1),
        Token(Token.Type.FOR, "for", 1),
        Token(Token.Type.FUN, "fun", 1),
        Token(Token.Type.IF, "if", 1),
        Token(Token.Type.NIL, "nil", 1),
        Token(Token.Type.OR, "or", 1),
        Token(Token.Type.RETURN, "return", 1),
        Token(Token.Type.SUPER, "super", 1),
        Token(Token.Type.THIS, "this", 1),
        Token(Token.Type.TRUE, "true", 1),
        Token(Token.Type.VAR, "var", 1),
        Token(Token.Type.WHILE, "while", 1),
        Token(Token.Type.EOF, "", 19),
      )
    ),
    Pair(
      "scanner_numbers.lox",
      listOf(
        Token(Token.Type.NUMBER, "123", 1),
        Token(Token.Type.NUMBER, "123.456", 2),
        Token(Token.Type.DOT, ".", 3),
        Token(Token.Type.NUMBER, "456", 3),
        Token(Token.Type.NUMBER, "123", 4),
        Token(Token.Type.DOT, ".", 4),
        Token(Token.Type.EOF, "", 13)
      )
    ),
    Pair(
      "scanner_punctuators.lox",
      listOf(
        Token(Token.Type.LEFT_PAREN, "(", 1),
        Token(Token.Type.RIGHT_PAREN, ")", 1),
        Token(Token.Type.LEFT_BRACE, "{", 1),
        Token(Token.Type.RIGHT_BRACE, "}", 1),
        Token(Token.Type.SEMICOLON, ";", 1),
        Token(Token.Type.COMMA, ",", 1),
        Token(Token.Type.PLUS, "+", 1),
        Token(Token.Type.MINUS, "-", 1),
        Token(Token.Type.STAR, "*", 1),
        Token(Token.Type.BANG_EQUAL, "!=", 1),
        Token(Token.Type.EQUAL_EQUAL, "==", 1),
        Token(Token.Type.LESS_EQUAL, "<=", 1),
        Token(Token.Type.GREATER_EQUAL, ">=", 1),
        Token(Token.Type.BANG_EQUAL, "!=", 1),
        Token(Token.Type.LESS, "<", 1),
        Token(Token.Type.GREATER, ">", 1),
        Token(Token.Type.SLASH, "/", 1),
        Token(Token.Type.DOT, ".", 1),
        Token(Token.Type.EOF, "", 22),
      ),
    ),
    Pair(
      "scanner_strings.lox",
      listOf(
        Token(Token.Type.STRING, "", 1),
        Token(Token.Type.STRING, "string", 2),
        Token(Token.Type.EOF, "", 7),
      )
    ),
    Pair(
      "scanner_whitespace.lox",
      listOf(
        Token(Token.Type.IDENTIFIER, "space", 1),
        Token(Token.Type.IDENTIFIER, "tabs", 1),
        Token(Token.Type.IDENTIFIER, "newlines", 1),
        Token(Token.Type.IDENTIFIER, "end", 6),
        Token(Token.Type.EOF, "", 13),
      )
    )
  )
  @Test
  fun testTokenization() {
    cases.forEach{
      case ->
        val text = this::class.java.classLoader.getResource(case.first)!!.readText()
        val scanner = Scanner(text)
        val actual = scanner.scan()
        assertEquals(case.second, actual)
    }
  }
}
