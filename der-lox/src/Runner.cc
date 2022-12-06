
#include <filesystem>
#include <iostream>
#include <sstream>

#include "Runner.h"
#include "Vm.h"


namespace lox {

using namespace __internal__;


Runner::Runner() {
  vm = new Vm();
}

Runner::~Runner() {
  delete vm;
}

void Runner::run_repl() {
  std::cout << "Lox REPL.\n";
  std::cout << "Type \\q to exit.\n";
  std::cout << "> ";
  std::string input;
  std::cin >> input;
  while (input != "\\q") {
    vm->interpret(input);
    std::cout << "> ";
    std::cin >> input;
  }
}

void Runner::run_file(const std::string& path) {
  std::filesystem::path file_path(path);
  if (!std::filesystem::exists(file_path)) {
    std::cerr << "File [" << path << "] not found.\n";
    std::exit(74);
  }

  std::cout << "Der Lox: Running from file: " << path << "\n";

  std::string code;
  __util__::read_from_file(path, &code);

  InterpretResult result = vm->interpret(code);

  if (result == InterpretResult::COMPILE_ERROR) {
    std::exit(65);
  }

  if (result == InterpretResult::RUNTIME_ERROR) {
    std::exit(70);
  }

}

} // namespace lox
