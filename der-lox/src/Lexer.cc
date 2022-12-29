

#include "Lexer.h"

#include <cctype>
#include <string>
#include <unordered_map>
#include <vector>

#include "Token.h"


namespace lox {
namespace __internal__ {

const std::unordered_map<std::string, Token::Type> SYMBOLIC_TOKENS = {
  {"!=", Token::Type::BANG_EQUAL},
  {"==", Token::Type::EQUAL_EQUAL},
  {">=", Token::Type::GREATER_EQUAL},
  {"<=", Token::Type::LESSER_EQUAL},
  {",", Token::Type::COMMA},
  {".", Token::Type::DOT},
  {"-", Token::Type::MINUS},
  {"+", Token::Type::PLUS},
  {";", Token::Type::SEMI},
  {"/", Token::Type::SLASH},
  {"*", Token::Type::STAR},
  {"(", Token::Type::LPAR},
  {")", Token::Type::RPAR},
  {"{", Token::Type::LCURL},
  {"}", Token::Type::RCURL},
  {"!", Token::Type::BANG},
  {"=", Token::Type::EQUAL},
  {">", Token::Type::GREATER},
  {"<", Token::Type::LESSER},
};

const std::unordered_map<std::string, Token::Type> KEYWORDS = {
  {"and", Token::Type::AND},
  {"class", Token::Type::CLASS},
  {"else", Token::Type::ELSE},
  {"false", Token::Type::FALSE},
  {"fun", Token::Type::FUN},
  {"for", Token::Type::FOR},
  {"if", Token::Type::IF},
  {"nil", Token::Type::NIL},
  {"or", Token::Type::OR},
  {"print", Token::Type::PRINT},
  {"return", Token::Type::RETURN},
  {"super", Token::Type::SUPER},
  {"this", Token::Type::THIS},
  {"true", Token::Type::TRUE},
  {"var", Token::Type::VAR},
  {"while", Token::Type::WHILE},
};

static bool is_good_identifier_char(char c) {
  return std::isalpha(c) || std::isdigit(c) || c == '_';
}

static bool is_space(char ch) {
  unsigned char uch = static_cast<unsigned char>(ch);
  return std::isspace(uch);
}

Lexer::Lexer(const std::string& code) :
code(code),
pos(0),
col(1),
line(1) {}

Lexer::~Lexer() {}

bool Lexer::scan(std::vector<Token>* tokens) {
  while (!this->is_at_end()) {
    if (is_space(this->peek())) {
      this->skip_whitespace();
      continue;
    }

    if (this->peek() == '/' && this->peek_next() == '/') {
      this->skip_comment();
      continue;
    }

    if (this->is_lox_symbol()) {
      tokens->push_back(this->parse_symbolic_token());
      continue;
    }

    if (this->is_number_start()) {
      tokens->push_back(this->parse_number_literal());
      continue;
    }

    if (this->is_string_start()) {
      tokens->push_back(this->parse_string_literal());
      continue;
    }

    if (this->is_identifier_start()) {
      tokens->push_back(this->parse_identifier_or_keyword());
      continue;
    }

    tokens->push_back(Token(
      Token::Type::ERROR,
      this->code.substr(pos, 1),
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

void Lexer::skip_whitespace() {
  if (this->peek() == '\n') {
    proceed();
    this->new_line();
    return;
  }

  proceed();
}

void Lexer::skip_comment() {
  while(this->peek() != '\n' && !this->is_at_end()) {
    proceed();
  }
}

Token Lexer::parse_symbolic_token() {
  std::string two = this->code.substr(this->pos, 2);

  if (SYMBOLIC_TOKENS.find(two) != SYMBOLIC_TOKENS.end()) {
    int start_col = this->col;
    proceed();
    proceed();
    return Token(
      SYMBOLIC_TOKENS.at(two),
      two,
      this->line,
      start_col
    );
  }

  std::string one = this->code.substr(this->pos, 1);

  if (SYMBOLIC_TOKENS.find(one) != SYMBOLIC_TOKENS.end()) {
    int start_col = this->col;
    proceed();

    return Token(
      SYMBOLIC_TOKENS.at(one),
      one,
      this->line,
      start_col
    );
  }

  proceed();
  int start_col = this->col;
  return Token(Token::Type::ERROR, "Unknown symbol.", this->line, start_col);
}

Token Lexer::parse_number_literal() {
  std::string numval = "";
  int start = this->col;
  while (std::isdigit(this->code[pos]) && !this->is_at_end()) {
    numval += this->code[pos];
    proceed();
  }

  if (this->code[pos] != '.') {
    return Token(Token::Type::NUMBER, numval, this->line, start);
  }

  numval.append(".");
  proceed();

  if (!std::isdigit(this->code[pos])) {
    return Token(
      Token::Type::ERROR,
      "Digit expected at line %d pos %d.",
      this->line,
      start
    );
  }

  while (std::isdigit(this->code[pos]) && !this->is_at_end()) {
    numval += this->code[pos];
    proceed();
  }

  return Token(Token::Type::NUMBER, numval, this->line, start);
}

Token Lexer::parse_string_literal() {
  int start_col = this->col;
  int start_line = this->line;
  proceed();

  std::string string_val = "";

  while (this->peek() != '"' && !this->is_at_end()) {
    string_val += this->peek();

    if (this->code[pos] == '\n') {
      this->new_line();
    }

    proceed();
  }

  if (this->is_at_end()) {
    proceed();
    return Token(
      Token::Type::ERROR,
      "Unterminated string.",
      this->line,
      this->col
    );
  }

  proceed(); // 'Eat' closing quotes;

  return Token(Token::Type::STRING, string_val, start_line, start_col);
}

Token Lexer::parse_identifier_or_keyword() {
  int start_col = this->col;
  std::string identifier = "";

  while (is_good_identifier_char(this->peek()) && !this->is_at_end()) {
    identifier += this->peek();
    proceed();
  }

  if (KEYWORDS.find(identifier) != KEYWORDS.end()) {
    return Token(KEYWORDS.at(identifier), identifier, this->line, start_col);
  }

  return Token(Token::Type::IDENTIFIER, identifier, this->line, start_col);
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
  return std::isdigit(this->peek());
}

bool Lexer::is_string_start() const {
  return this->peek() == '"';
}

bool Lexer::is_identifier_start() const {
  return std::isalpha(this->peek()) || this->peek() == '_';
}

char Lexer::peek() const {
  if (pos >= this->code.length()) {
    return '\0';
  }

  return this->code[pos];
}

char Lexer::peek_next() const {
  if (this->is_at_end() || this->pos >= this->code.size() - 1) {
    return '\0';
  }
  return this->code[pos + 1];
}

} // namespace __internal__
} // namespace lox
