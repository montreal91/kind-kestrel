
#include <filesystem>
#include <iostream>

#include "Runner.h"

namespace lox {

void Runner::run_repl() {
  std::cout << "Lox REPL.\n";
}

void Runner::run_file(const std::string& path) {
  std::filesystem::path file_path(path);
  if (!std::filesystem::exists(file_path)) {
    std::cerr << "File [" << path << "] not found.\n";
    std::exit(74);
  }

  std::cout << "CCLox: Running from file: " << path << "\n";
}

} // namespace lox
