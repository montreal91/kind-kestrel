
#include "Token.h"


namespace lox {
namespace __internal__ {

static std::string type_to_string(Token::Type type) {
  switch (type) {
    case Token::Type::COMMA: return "COMMA";
    case Token::Type::DOT: return "DOT";
    case Token::Type::MINUS: return "MINUS";
    case Token::Type::PLUS: return "PLUS";
    case Token::Type::SEMI: return "SEMI";
    case Token::Type::SLASH: return "SLASH";
    case Token::Type::STAR: return "STAR";
    case Token::Type::LPAR: return "LPAR";
    case Token::Type::RPAR: return "RPAR";
    case Token::Type::LCURL: return "LCURL";
    case Token::Type::RCURL: return "RCURL";
    case Token::Type::BANG: return "BANG";
    case Token::Type::BANG_EQUAL: return "BANG_EQUAL";
    case Token::Type::EQUAL: return "EQUAL";
    case Token::Type::EQUAL_EQUAL: return "EQUAL_EQUAL";
    case Token::Type::GREATER: return "GREATER";
    case Token::Type::GREATER_EQUAL: return "GREATER_EQUAL";
    case Token::Type::LESSER: return "LESSER";
    case Token::Type::LESSER_EQUAL: return "LESSER_EQUAL";

    case Token::Type::NUMBER: return "NUMBER";
    case Token::Type::STRING: return "STRING";
    case Token::Type::IDENTIFIER: return "IDENTIFIER";

    case Token::Type::AND: return "AND";
    case Token::Type::CLASS: return "CLASS";
    case Token::Type::ELSE: return "ELSE";
    case Token::Type::FALSE: return "FALSE";
    case Token::Type::FUN: return "FUN";
    case Token::Type::FOR: return "FOR";
    case Token::Type::IF: return "IF";
    case Token::Type::NIL: return "NIL";
    case Token::Type::OR: return "OR";
    case Token::Type::PRINT: return "PRINT";
    case Token::Type::RETURN: return "RETURN";
    case Token::Type::SUPER: return "SUPER";
    case Token::Type::THIS: return "THIS";
    case Token::Type::TRUE: return "TRUE";
    case Token::Type::VAR: return "VAR";
    case Token::Type::WHILE: return "WHILE";

    case Token::Type::ERROR: return "ERROR";
    case Token::Type::END_OF_FILE: return "END_OF_FILE";
    default: return "UNKNOWN"; // Unreachable
  }
}


Token::Token(Token::Type type, const std::string& value, int line, int column) :
type(type),
value(value),
column(column),
line(line) {}

Token::~Token() {}

std::string Token::__str__() const {
  std::string res = "Token ";
  res.append(type_to_string(this->type));
  res.append(" [");
  res.append(this->value);
  res.append("] ");
  res.append("(");
  res.append(std::to_string(this->line));
  res.append(", ");
  res.append(std::to_string(this->column));
  res.append(")");
  return res;
}

} // namespace __internal__
} // namespace lox
