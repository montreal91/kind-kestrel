package org.example.rlc.frontend

/**
 * Represents a lexical token produced by a scanner or parser during the lexical analysis phase.
 *
 * A token is identified by its type, its literal value (if applicable), and the line number
 * from which the token was extracted in the source code. The `Token` class is immutable
 * and provides overrides for common operations such as equality comparison and hashing.
 *
 * @property type The type of the token, represented by the [Type] enum.
 *                This defines the category or meaning of the token (e.g., identifier, operator, literal).
 * @property value The string representation or literal value associated with the token.
 *                 This will often be the raw text from the source code.
 * @property lineNumber The line number in the source code where the token was encountered.
 *                      This is useful for error reporting or debugging.
 */
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
