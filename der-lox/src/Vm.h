
#ifndef DERLOX_VM_H
#define DERLOX_VM_H

#include <string>

#include "Common.h"


namespace lox {
namespace __internal__ {


class Vm {
public:
  Vm();
  ~Vm();

  InterpretResult interpret(const std::string& code);
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_VM_H
