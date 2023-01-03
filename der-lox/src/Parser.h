
#ifndef DERLOX_PARSER_H
#define DERLOX_PARSER_H


#include <vector>

#include "Statement.h"
#include "Token.h"


namespace lox {
namespace __internal__ {


class Parser {
public:
  bool parse(
      const std::vector<Token>& tokens,
      std::vector<Statement>* statements
  );
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_PARSER_H
