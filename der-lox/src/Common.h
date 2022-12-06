
#ifndef DERLOX_COMMON_H
#define DERLOX_COMMON_H

#include <fstream>
#include <string>


namespace lox {
namespace __internal__ {

enum InterpretResult {
  OK,
  COMPILE_ERROR,
  RUNTIME_ERROR,
};

} // namespace __internal__
} // namespace lox

namespace __util__ {
void read_from_file(const std::string& path, std::string* text);
} // namespace __util__


#endif // DERLOX_COMMON_H
