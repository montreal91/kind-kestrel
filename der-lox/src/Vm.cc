
#include <iostream>
#include <string>
#include <vector>

#include "Vm.h"


namespace lox {
namespace __internal__ {


Vm::Vm() {
  std::cout << "Der Lox: Initializing VM.\n";
}

Vm::~Vm() {
  std::cout << "Der Lox: Freeing VM resources.\n";
}

InterpretResult Vm::interpret(Chunk* chunk) {
  return InterpretResult::OK;
}

} // namespace __internal__
} // namespace lox
