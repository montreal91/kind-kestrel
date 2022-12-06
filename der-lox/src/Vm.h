
#ifndef DERLOX_VM_H
#define DERLOX_VM_H

#include <string>

#include "Chunk.h"
#include "Common.h"


namespace lox {
namespace __internal__ {


class Vm {
public:
  Vm();
  ~Vm();

  InterpretResult interpret(Chunk* bytecode);
};

} // namespace __internal__
} // namespace lox

#endif // DERLOX_VM_H
