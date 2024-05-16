package org.example.rlc.frontend

private val keywords = mapOf(
  Pair("and", Token.Type.AND),
  Pair("class", Token.Type.CLASS),
  Pair("else", Token.Type.ELSE),
  Pair("false", Token.Type.FALSE),
  Pair("for", Token.Type.FOR),
  Pair("fun", Token.Type.FUN),
  Pair("if", Token.Type.IF),
  Pair("nil", Token.Type.NIL),
  Pair("or", Token.Type.OR),
  Pair("print", Token.Type.PRINT),
  Pair("return", Token.Type.RETURN),
  Pair("super", Token.Type.SUPER),
  Pair("this", Token.Type.THIS),
  Pair("true", Token.Type.TRUE),
  Pair("var", Token.Type.VAR),
  Pair("while", Token.Type.WHILE),
)

private val punctuatorStart = mapOf(
  Pair('+', Token.Type.PLUS),
  Pair('-', Token.Type.MINUS),
  Pair('*', Token.Type.STAR),
  Pair('(', Token.Type.LEFT_PAREN),
  Pair(')', Token.Type.RIGHT_PAREN),
  Pair('{', Token.Type.LEFT_BRACE),
  Pair('}', Token.Type.RIGHT_BRACE),
  Pair(',', Token.Type.COMMA),
  Pair(';', Token.Type.SEMICOLON),
  Pair('.', Token.Type.DOT),
  Pair('!', Token.Type.BANG),
  Pair('=', Token.Type.EQUAL),
  Pair('>', Token.Type.GREATER),
  Pair('<', Token.Type.LESS),
)

private val doubleCharPunctuators = mapOf(
  Pair('!', Token.Type.BANG_EQUAL),
  Pair('=', Token.Type.EQUAL_EQUAL),
  Pair('>', Token.Type.GREATER_EQUAL),
  Pair('<', Token.Type.LESS_EQUAL),
)

private fun Char.isIdentifierStart(): Boolean = this == '_' || this.isLetter()

private fun Char.isPunctuatorStart(): Boolean = this in punctuatorStart.keys

private fun Char.isDoubleCharPunctuator(): Boolean = this in doubleCharPunctuators.keys

class Scanner(private val text: String) {
  val hasErrors get() = errors.isNotEmpty()

  private val errors: MutableList<LoxCompileError> = mutableListOf()
  private val tokens: MutableList<Token> = mutableListOf()

  private var currentChar = 0

  private var currentLineNumber = 1

  private val atEnd get() = currentChar == text.length
  private val isLastChar get() = currentChar == text.length - 1

  private val peek get() = when (atEnd) {
    true -> 0.toChar()
    false -> text[currentChar]
  }

  private val peekNext get() = when (atEnd or isLastChar) {
    true -> 0.toChar()
    false -> text[currentChar + 1]
  }

  fun getErrors(): List<LoxCompileError> = errors.toList()

  fun scan(): List<Token> {
    while (!atEnd) {
      skipWhitespace()

      when {
        peek == '/' -> {
          if (peekNext == '/') {
            skipComment()
          }
          else {
            tokens.add(Token(Token.Type.SLASH, peek.toString(), currentLineNumber))
            advance()
          }
        }
        peek.isPunctuatorStart() -> parsePunctuator()
        peek == '"' -> parseString()
        peek == 0.toChar() -> continue
        peek.isDigit() -> parseNumber()
        peek.isIdentifierStart() -> parseKeyWordOrIdentifier()
        else -> {
          tokens.add(Token(Token.Type.ERROR, peek.toString(), currentLineNumber))
          errors.add(LoxCompileError(
            message = "Unexpected character: $peek.",
            token = Token(Token.Type.ERROR, peek.toString(), currentLineNumber)
          ))
          advance()
        }
      }
    }

    tokens.add(Token(Token.Type.EOF, "", currentLineNumber))

    return tokens.toList()
  }

  private fun parsePunctuator() = when {
    peek.isDoubleCharPunctuator() -> parseDoubleCharPunctuator()
    else -> parseSingleCharPunctuator()
  }

  private fun parseDoubleCharPunctuator() {
    if (peekNext == '=') {
      tokens.add(Token(doubleCharPunctuators[peek]!!, "$peek$peekNext", currentLineNumber))
      advance()
      advance()
      return
    }
    parseSingleCharPunctuator()
  }

  private fun parseSingleCharPunctuator() {
    tokens.add(Token(punctuatorStart[peek]!!, "$peek", currentLineNumber))
    advance()
  }

  private fun parseNumber() {
    val start = currentChar
    while (!atEnd && peek.isDigit()) {
      advance()
    }
    if (peek == '.' && peekNext.isDigit()) {
      advance()
      while (!atEnd && peek.isDigit()) {
        advance()
      }
    }

    tokens.add(Token(
      type = Token.Type.NUMBER,
      value = text.substring(start, currentChar),
      lineNumber = currentLineNumber
    ))
  }

  private fun parseKeyWordOrIdentifier() {
    val start = currentChar
    while (!atEnd && (peek.isLetterOrDigit() || peek == '_')) {
      advance()
    }
    val value = text.substring(start, currentChar)

    when (keywords.containsKey(value)) {
      true -> tokens.add(Token(type = keywords[value]!!, value, currentLineNumber))
      false -> tokens.add(Token(type = Token.Type.IDENTIFIER, value, currentLineNumber))
    }
  }

  private fun parseString() {
    val start = currentChar + 1
    advance() // skip the first comma

    while (!atEnd && peek != '\n' && peek != '"') {
      advance()
    }

    if (atEnd || peek == '\n') {
      tokens.add(Token(type = Token.Type.ERROR, value =  "", lineNumber = currentLineNumber))
      errors.add(LoxCompileError(
        message = "Unterminated string.",
        token = Token(Token.Type.ERROR, peek.toString(), currentLineNumber)
      ))
    }
    else if (peek == '"') {
      tokens.add(Token(Token.Type.STRING, text.substring(start, currentChar), currentLineNumber))
    }
    advance() // consume the last comma
  }

  private fun skipWhitespace() {
    while (!atEnd && peek.isWhitespace()) {
      advance()
    }
  }

  private fun advance() {
    if (peek == '\n') {
      currentLineNumber++
    }
    currentChar++
  }

  private fun skipComment() {
    while (!atEnd && peek != '\n') {
      advance()
    }

    // I think I have to do this
    advance()
  }
}
