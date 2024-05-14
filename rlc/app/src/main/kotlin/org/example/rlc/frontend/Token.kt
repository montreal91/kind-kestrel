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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as Token
    if (type != other.type) return false
    if (value != other.value) return false
    if (lineNumber != other.lineNumber) return false
    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + value.hashCode()
    result = 31 * result + lineNumber
    return result
  }
}
