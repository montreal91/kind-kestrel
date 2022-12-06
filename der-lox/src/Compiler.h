
#ifndef DERLOX_COMPILER_H
#define DERLOX_COMPILER_H

// #include <cstdint>
// #include <string>
#include <vector>

#include "Chunk.h"
#include "Statement.h"


namespace lox {
namespace __internal__ {


class Compiler {
public:
  bool compile(
    const std::vector<Statement>& parse_tree,
    Chunk* chunk
  );
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_COMPILER_H
