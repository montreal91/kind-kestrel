
#include <iostream>
#include <string>

#include "Runner.h"


int main(int argc, const char* argv[]) {
  lox::Runner* runner = new lox::Runner();

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
