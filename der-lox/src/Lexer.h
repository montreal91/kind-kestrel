
#ifndef DERLOX_LEXER_H
#define DERLOX_LEXER_H

#include <string>
#include <vector>

#include "Token.h"


namespace lox {
namespace __internal__ {

class Lexer {
public:
  bool scan(const std::string& code, std::vector<Token>* tokens);
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_LEXER_H
