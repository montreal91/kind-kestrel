
#include <iostream>
#include <string>
#include <vector>

#include "Printable.h"

namespace lox {
namespace __internal__ {


void print(const Printable& printable, const std::string& endl) {
  // I'm not interested into printing printables into other streams, so...
  std::cout << printable.__str__() << endl;
}

void print(const Printable& printable) {
  print(printable, "\n");
}

} // namespace __internal__
} // namespace lox
