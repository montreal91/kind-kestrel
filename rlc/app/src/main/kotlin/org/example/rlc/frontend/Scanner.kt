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

private val singleSymbolTokens = mapOf(
  Pair('+', Token.Type.PLUS),
)

private fun Char.isIdentifierStart(): Boolean = when {
  (this == '_' || this.isLetter()) -> true
  else -> false
}

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
        peek == '(' -> {
          tokens.add(Token(Token.Type.LEFT_PAREN, peek.toString(), currentLineNumber))
          advance()
        }
        peek == ')' -> {
          tokens.add(Token(Token.Type.RIGHT_PAREN, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '{' -> {
          tokens.add(Token(Token.Type.LEFT_BRACE, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '}' -> {
          tokens.add(Token(Token.Type.RIGHT_BRACE, peek.toString(), currentLineNumber))
          advance()
        }
        peek == ',' -> {
          tokens.add(Token(Token.Type.COMMA, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '.' -> {
          tokens.add(Token(Token.Type.DOT, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '-' -> {
          tokens.add(Token(Token.Type.MINUS, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '+' -> {
          tokens.add(Token(Token.Type.PLUS, peek.toString(), currentLineNumber))
          advance()
        }
        peek == ';' -> {
          tokens.add(Token(Token.Type.SEMICOLON, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '*' -> {
          tokens.add(Token(Token.Type.STAR, peek.toString(), currentLineNumber))
          advance()
        }
        peek == '!' -> {
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.BANG_EQUAL, "!=", currentLineNumber))
            advance()
            advance()
          } else {
            tokens.add(Token(Token.Type.BANG, "!", currentLineNumber))
            advance()
          }
        }
        peek == '=' -> {
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.EQUAL_EQUAL, "==", currentLineNumber))
            advance()
            advance()
          } else {
            tokens.add(Token(Token.Type.EQUAL, "=", currentLineNumber))
            advance()
          }
        }
        peek == '>' -> {
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.GREATER_EQUAL, ">=", currentLineNumber))
            advance()
            advance()
          } else {
            tokens.add(Token(Token.Type.GREATER, ">", currentLineNumber))
            advance()
          }
        }
        peek == '<' -> {
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.LESS_EQUAL, "<=", currentLineNumber))
            advance()
            advance()
          } else {
            tokens.add(Token(Token.Type.LESS, "<", currentLineNumber))
            advance()
          }
        }
        peek == '"' -> parseString()
        peek == 0.toChar() -> continue
        peek.isDigit() -> parseNumber()
        peek.isIdentifierStart() -> parseAlphaNumericToken()
        else -> {
          tokens.add(Token(Token.Type.ERROR, peek.toString(), currentLineNumber))
          errors.add(LoxCompileError(
            message = "Unexpected character: $peek.",
            lineNumber = currentLineNumber
          ))
          advance()
        }
      }
    }

    tokens.add(Token(Token.Type.EOF, "", currentLineNumber))

    return tokens.toList()
  }

  private fun parseAlphaNumericToken() {
    if (peek.isDigit()) {
      parseNumber()
    } else {
      parseKeyWordOrIdentifier()
    }
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
        message = "Unexpected end of the string literal.",
        lineNumber = currentLineNumber
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
