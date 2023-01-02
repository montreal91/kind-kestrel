
#include <filesystem>
#include <iostream>
#include <sstream>

#include "Interpreter.h"

#include "Chunk.h"
#include "Compiler.h"
#include "Lexer.h"
#include "Parser.h"
#include "Statement.h"
#include "Token.h"
#include "Vm.h"


namespace lox {

using namespace __internal__;


Interpreter::Interpreter() {
  vm = new Vm();
}

Interpreter::~Interpreter() {
  delete vm;
}

void Interpreter::run_repl() {
  std::cout << "Lox REPL.\n";
  std::cout << "Type \\q to exit.\n";
  std::cout << "> ";
  std::string input;
  std::getline(std::cin, input);

  while (input != "\\q") {
    run(input);
    std::cout << "> ";
    std::getline(std::cin, input);
  }
}

void Interpreter::run_file(const std::string& path) {
  std::filesystem::path file_path(path);
  if (!std::filesystem::exists(file_path)) {
    std::cerr << "File [" << path << "] not found.\n";
    std::exit(74);
  }

  std::cout << "Der Lox: Running from file: " << path << "\n";

  std::string code;
  __util__::read_from_file(path, &code);

  InterpretResult result = run(code);

  if (result == InterpretResult::COMPILE_ERROR) {
    std::exit(65);
  }

  if (result == InterpretResult::RUNTIME_ERROR) {
    std::exit(70);
  }
}

InterpretResult Interpreter::run(const std::string& code) {
  std::cout << "Der Lox: Running.\n";
  std::cout << "===================================\n";
  std::cout << code << "\n";
  std::cout << "===================================\n";

  // Lexing
  std::vector<Token> tokens;
  Lexer lexer(code);
  lexer.scan(&tokens);

  std::cout << "Der Lox: Tokens:\n";
  for (Token& token : tokens) {
    print(token);
  }

  // Parsing
  std::vector<Statement> statements;
  Parser parser;
  parser.parse(tokens, &statements);

  // Operations on the AST

  // Compiling
  Chunk chunk;
  Compiler compiler;
  compiler.compile(statements, &chunk);

  // Interpretation
  InterpretResult result = vm->interpret(&chunk);

  return result;
}

} // namespace lox
