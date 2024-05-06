package org.example.rlc.frontend

class Token(val type: Type, val value: String, val lineNumber: Int) {
  enum class Type {
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS,
    SEMICOLON, SLASH, STAR,

    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    IDENTIFIER, STRING, NUMBER,

    AND, OR, CLASS, ELSE, FALSE,
    FOR, FUN, IF, NIL,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    ERROR, EOF
  }

  override fun toString(): String {
    return "Token [type=$type, value='$value', lineNumber=$lineNumber]"
  }
}
