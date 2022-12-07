
#ifndef DERLOX_INTERPRETER_H
#define DERLOX_INTERPRETER_H

#include <string>

#include "Common.h"
#include "Vm.h"

namespace lox {

using namespace __internal__;

class Interpreter {
public:
  Interpreter();
  ~Interpreter();

  void run_repl();
  void run_file(const std::string& path);

private:
  Vm* vm;

  InterpretResult run(const std::string& code);
};

} // namespace lox

#endif // DERLOX_INTERPRETER_H
