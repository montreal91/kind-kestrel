
#ifndef DERLOX_LEXER_H
#define DERLOX_LEXER_H

#include <string>
#include <unordered_map>
#include <vector>

#include "Token.h"


namespace lox {
namespace __internal__ {

class Lexer {
public:
  Lexer(const std::string& code);
  ~Lexer();

  // I admit that this interface is kinda weird,
  // but I don't want to mess with shared pointers at the moment
  bool scan(std::vector<Token>* tokens);


private:
  size_t pos;
  size_t line;
  size_t col;

  const std::string& code;


  // void init_lexer(const std::string& code);
  void proceed();
  void new_line();
  void skip_whitespace();
  void skip_comment();

  char peek() const;
  char peek_next() const;

  Token parse_symbolic_token();
  Token parse_number_literal();
  Token parse_string_literal();
  Token parse_identifier_or_keyword();

  bool is_at_end() const;
  bool is_lox_symbol() const;
  bool is_number_start() const;
  bool is_string_start() const;
  bool is_identifier_start() const;
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_LEXER_H
