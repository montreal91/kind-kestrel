
#ifndef DERLOX_PRINTABLE_H
#define DERLOX_PRINTABLE_H


#include <string>


namespace lox {
namespace __internal__ {


class Printable {
public:
  virtual std::string __str__() const = 0;
};

void print(const Printable& printable);
void print(const Printable& printable, const std::string& endl);

} // namespace __internal__
} // namespace lox

#endif // DERLOX_PRINTABLE_H
