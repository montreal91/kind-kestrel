
#include <iostream>
#include <string>

#include "Vm.h"

namespace lox {
namespace __internal__ {

Vm::Vm() {
  std::cout << "Der Lox: Initializing VM.\n";
}

Vm::~Vm() {
  std::cout << "Der Lox: Freeing VM resources.\n";
}

InterpretResult Vm::interpret(const std::string& code) {
  std::cout << "Der Lox: Interpreting the Lox Code.\n\n";
  std::cout << "===================================\n";
  std::cout << code << "\n";
  std::cout << "===================================\n";
  return InterpretResult::OK;
}

} // namespace __internal__
} // namespace lox
