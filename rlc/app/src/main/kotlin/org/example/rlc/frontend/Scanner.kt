package org.example.rlc.frontend

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

      when (peek) {
        '/' -> {
          if (peekNext == '/') {
            skipComment()
          }
          else {
            tokens.add(Token(Token.Type.SLASH, peek.toString(), currentLineNumber))
            advance()
          }
        }
        '(' -> {
          tokens.add(Token(Token.Type.LEFT_PAREN, peek.toString(), currentLineNumber))
          advance()
        }
        ')' -> {
          tokens.add(Token(Token.Type.RIGHT_PAREN, peek.toString(), currentLineNumber))
          advance()
        }
        '{' -> {
          tokens.add(Token(Token.Type.LEFT_BRACE, peek.toString(), currentLineNumber))
          advance()
        }
        '}' -> {
          tokens.add(Token(Token.Type.RIGHT_BRACE, peek.toString(), currentLineNumber))
          advance()
        }
        ',' -> {
          tokens.add(Token(Token.Type.COMMA, peek.toString(), currentLineNumber))
          advance()
        }
        '.' -> {
          tokens.add(Token(Token.Type.DOT, peek.toString(), currentLineNumber))
          advance()
        }
        '-' -> {
          tokens.add(Token(Token.Type.MINUS, peek.toString(), currentLineNumber))
          advance()
        }
        '+' -> {
          tokens.add(Token(Token.Type.PLUS, peek.toString(), currentLineNumber))
          advance()
        }
        ';' -> {
          tokens.add(Token(Token.Type.SEMICOLON, peek.toString(), currentLineNumber))
          advance()
        }
        '*' -> {
          tokens.add(Token(Token.Type.STAR, peek.toString(), currentLineNumber))
          advance()
        }
        '!' -> {
          advance()
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.BANG_EQUAL, "!=", currentLineNumber))
            advance()
          } else {
            tokens.add(Token(Token.Type.BANG, "!", currentLineNumber))
          }
        }
        '=' -> {
          advance()
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.EQUAL_EQUAL, "==", currentLineNumber))
            advance()
          } else {
            tokens.add(Token(Token.Type.EQUAL, "=", currentLineNumber))
          }
        }
        '>' -> {
          advance()
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.GREATER_EQUAL, ">=", currentLineNumber))
            advance()
          } else {
            tokens.add(Token(Token.Type.GREATER, ">", currentLineNumber))
          }
        }
        '<' -> {
          advance()
          if (peekNext == '=') {
            tokens.add(Token(Token.Type.LESS_EQUAL, "<=", currentLineNumber))
            advance()
          } else {
            tokens.add(Token(Token.Type.LESS, "<", currentLineNumber))
          }
        }
        '"' -> parseString()
        else -> parseAlphaNumericToken()
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
  }

  private fun parseKeyWordOrIdentifier() {
  }

  private fun parseString() {
    val start = currentChar + 1
    advance() // skip the first comma

    while (!atEnd && peek != '\n' && peek != '"') {
      advance()
    }

    if (atEnd || peek == '\n') {
      tokens.add(Token(Token.Type.ERROR, "", currentLineNumber))
      errors.add(LoxCompileError("Unexpected end of the string literal.", currentLineNumber))
    }
    else if (peek == '"') {
      tokens.add(Token(Token.Type.STRING, text.substring(start, currentChar), currentLineNumber))
    }
    advance()
  }

  private fun match(expected: Char): Boolean {
    if (atEnd) {
      return false;
    }

    if (peek != expected) {
      return false
    }

    advance()
    return true
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
