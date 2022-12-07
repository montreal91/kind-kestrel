
#include <iostream>
#include <string>

#include "Interpreter.h"


int main(int argc, const char* argv[]) {
  lox::Interpreter* runner = new lox::Interpreter();

  if (argc == 1) {
    runner->run_repl();
  }
  else if (argc == 2) {
    std::string path(argv[1]);
    runner->run_file(path);
  }
  else {
    std::cout << "Usage: DerLox [path/to/file.lox]\n";
  }

  delete runner;

  return 0;
}
