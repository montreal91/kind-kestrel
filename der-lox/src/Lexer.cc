

#include "Lexer.h"

#include <cctype>
#include <string>
#include <vector>

#include "Token.h"


namespace lox {
namespace __internal__ {

Lexer::Lexer(const std::string& code) :
code(code),
pos(0),
col(1),
line(1) {}

Lexer::~Lexer() {}

bool Lexer::scan(std::vector<Token>* tokens) {
  while (!this->is_at_end()) {
    this->skip();

    if (this->is_lox_symbol()) {
      tokens->push_back(this->parse_symbolic_token());
      continue;
    }

    if (this->is_number_start()) {
      tokens->push_back(this->parse_number_literal());
      proceed();
      continue;
    }

    tokens->push_back(Token(
      Token::Type::ERROR,
      std::to_string(this->code[this->pos]),
      this->line,
      this->col
    ));
    proceed();
  }

  tokens->push_back(Token(Token::Type::END_OF_FILE, "", this->line, this->col));
  return true;
}

void Lexer::new_line() {
  this->col = 1;
  this->line++;
}

void Lexer::proceed() {
  this->pos++;
  this->col++;
}

void Lexer::skip() {
  while (std::isspace(this->code[pos]) && !this->is_at_end()) {
    if (this->code[pos] == '\n') {
      this->new_line();
    }
    proceed();
  }
}

Token Lexer::parse_symbolic_token() {
  if (this->code[this->pos] == '+') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::PLUS, "+", this->line, ccol);
  }

  if (this->code[this->pos] == '-') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::MINUS, "-", this->line, ccol);
  }

  if (this->code[this->pos] == '*') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::STAR, "*", this->line, ccol);
  }

  if (this->code[this->pos] == '/') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::SLASH, "/", this->line, ccol);
  }

  if (this->code[this->pos] == '.') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::DOT, ".", this->line, ccol);
  }

  if (this->code[this->pos] == ',') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::COMMA, ",", this->line, ccol);
  }

  if (this->code[this->pos] == ';') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::SEMI, ";", this->line, ccol);
  }

  if (this->code[this->pos] == '(') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::LPAR, "(", this->line, ccol);
  }

  if (this->code[this->pos] == ')') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::RPAR, ")", this->line, ccol);
  }

  if (this->code[this->pos] == '{') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::LCURL, "{", this->line, ccol);
  }

  if (this->code[this->pos] == '}') {
    int ccol = this->col;
    proceed();
    return Token(Token::Type::RCURL, "}", this->line, ccol);
  }

  if (this->code[this->pos] == '!') {
    int ccol = this->col;
    if (this->code[this->pos + 1] == '='){
      this->proceed();
      this->proceed();
      return Token(Token::Type::BANG_EQUAL, "!=", this->line, ccol);
    }
    this->proceed();
    return Token(Token::Type::BANG, "!", this->line, ccol);
  }

  if (this->code[this->pos] == '=') {
    int ccol = this->col;
    if (this->code[this->pos + 1] == '='){
      this->proceed();
      this->proceed();
      return Token(Token::Type::EQUAL_EQUAL, "==", this->line, ccol);
    }
    this->proceed();
    return Token(Token::Type::EQUAL, "=", this->line, ccol);
  }

  if (this->code[this->pos] == '>') {
    int ccol = this->col;
    if (this->code[this->pos + 1] == '='){
      this->proceed();
      this->proceed();
      return Token(Token::Type::GREATER_EQUAL, ">=", this->line, ccol);
    }
    this->proceed();
    return Token(Token::Type::GREATER, ">", this->line, ccol);
  }

  if (this->code[this->pos] == '<') {
    int ccol = this->col;
    if (this->code[this->pos + 1] == '='){
      this->proceed();
      this->proceed();
      return Token(Token::Type::LESSER_EQUAL, "<=", this->line, ccol);
    }
    this->proceed();
    return Token(Token::Type::LESSER, "<", this->line, ccol);
  }
}

Token Lexer::parse_number_literal() {
  Token res(Token::Type::NUMBER, "-1", -1, -1);
  return res;
}

Token Lexer::parse_string_literal() {
  Token res(Token::Type::STRING, "-1", -1, -1);
  return res;
}

Token Lexer::parse_identifier_or_keyword() {
  Token res(Token::Type::NIL, "nil", -1, -1);
  return res;
}

bool Lexer::is_at_end() const {
  return this->pos >= this->code.size();
}

bool Lexer::is_lox_symbol() const {
  switch (this->code[pos]) {
    case ',': return true;
    case '.': return true;
    case '-': return true;
    case '+': return true;
    case ';': return true;
    case '/': return true;
    case '*': return true;
    case '(': return true;
    case ')': return true;
    case '{': return true;
    case '}': return true;
    case '!': return true;
    case '=': return true;
    case '>': return true;
    case '<': return true;
    default: return false;
  }
}

bool Lexer::is_number_start() const {
  return std::isdigit(this->code[pos]);
}

} // namespace __internal__
} // namespace lox
