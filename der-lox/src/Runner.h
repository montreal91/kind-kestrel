#ifndef DERLOX_RUNNER_H
#define DERLOX_RUNNER_H

#include <string>

namespace lox {

class Runner {
public:
  void run_repl();
  void run_file(const std::string& path);
};

} // namespace lox

#endif // DERLOX_RUNNER_H
