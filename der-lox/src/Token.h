
#ifndef DERLOX_TOKEN_H
#define DERLOX_TOKEN_H

#include <string>

#include "Printable.h"


namespace lox {
namespace __internal__ {


class Token : public Printable {
public:
  enum Type {
    COMMA, DOT, MINUS, PLUS, SEMI, SLASH, STAR,
    LPAR, RPAR, LCURL, RCURL,

    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESSER, LESSER_EQUAL,

    // Literals
    IDENTIFIER, NUMBER, STRING,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT,
    RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    END_OF_FILE, ERROR,
  };

  Token(Type type, const std::string& value, int row, int column);

  const Type type;
  const std::string value;
  const int row;
  const int column;

  virtual std::string __str__() const override;
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_TOKEN_H
