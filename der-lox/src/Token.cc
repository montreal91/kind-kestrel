
#include "Token.h"


namespace lox {
namespace __internal__ {

static std::string type_to_string(Token::Type type) {
  switch (type) {
    case Token::Type::COMMA: return "COMMA";
    default: return "UNKNOWN"; // Unreachable
  }
}


Token::Token(Token::Type type, const std::string& value, int row, int column) :
type(type),
value(value),
column(column),
row(row) {}

Token::~Token() {}

std::string Token::__str__() const {
  std::string res = "Token ";
  res.append(type_to_string(this->type));
  res.append(" [");
  res.append(this->value);
  res.append("]");

  return res;
}



} // namespace __internal__
} // namespace lox
