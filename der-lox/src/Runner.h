#ifndef DERLOX_RUNNER_H
#define DERLOX_RUNNER_H

#include <string>

#include "Common.h"
#include "Vm.h"

namespace lox {

using namespace __internal__;

class Runner {
public:
  Runner();
  ~Runner();

  void run_repl();
  void run_file(const std::string& path);

private:
  Vm* vm;
};

} // namespace lox

#endif // DERLOX_RUNNER_H
