
#include "Token.h"


namespace lox {
namespace __internal__ {

Token::Token(Token::Type type, const std::string& value, int row, int column) :
type(type),
value(value),
column(column),
row(row) {}

} // namespace internals
} // namespace lox
