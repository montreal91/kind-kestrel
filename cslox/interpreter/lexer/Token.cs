
namespace Lox;

enum TokenType {
    AND,
    CLASS,
    ELSE,
    FALSE,
    FUN,
    IF,
    NIL,
    OR,
    PRINT,
    RETURN,
    SUPER,
    THIS,
    TRUE,
    VAR,
    WHILE,
    PLUS,
    MINUS,
    MUL,
    DIV,
    NUMBER,
    STRING,
    IDENTIFIER,
    GREATER,
    LESSER,
    GREATER_OR_EQUAL,
    LESSER_OR_EQUAL,
    EQUAL,
    EQUAL_EQUAL,
    NOT_EQUAL,
    LPAR,
    RPAR,
    LCURL,
    RCURL,
    SEMI,
    PANG,
    DOT,
    COMMA,
    EOF
}

class Token {
    internal readonly TokenType type;
    internal readonly object? value;
    internal readonly int line;
    internal readonly int column;

    internal Token(TokenType type, object? value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public override int GetHashCode() => (type, value, line, column).GetHashCode();

    public override bool Equals(object? obj) {
        if (obj is null) {
            return false;
        }
        if (!(obj is Token)) {
            return false;
        }
        Token other = (Token) obj;

        bool typesAreEqual = this.type == other.type;
        bool valuesAreEqual = false;

        if (this.value is null) {
            valuesAreEqual = other.value is null;
        } else {
            valuesAreEqual = this.value.Equals(other.value);
        }

        bool linesAreEqual = this.line == other.line;
        bool columnsAreEqual = this.column == other.column;

        return typesAreEqual && valuesAreEqual && linesAreEqual && columnsAreEqual;
    }

    public static bool operator ==(Token? lhs, Token? rhs) {
        if (lhs is null) {
            if (rhs is null) {
                return true;
            }

            return false;
        }

        return lhs.Equals(rhs);
    }

    public static bool operator !=(Token? lhs, Token? rhs) => !(lhs == rhs);
}
