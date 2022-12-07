
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
  std::vector<Token>* tokens = new std::vector<Token>();
  Lexer* lexer = new Lexer();
  lexer->scan(code, tokens);
  delete lexer;

  // Parsing
  std::vector<Statement>* statements = new std::vector<Statement>();
  Parser* parser = new Parser();
  parser->parse(*tokens, statements);
  delete parser;

  // Operations on the AST
  // Compiling
  Chunk* chunk = new Chunk();
  Compiler* compiler = new Compiler();
  compiler->compile(*statements, chunk);
  delete compiler;

  // Remove redundancies
  // Everything is compiled and actually ready for interpretation.
  delete statements;
  delete tokens;

  // Interpretation
  InterpretResult result = vm->interpret(chunk);

  // Cleanup
  delete chunk;
  return result;
}

} // namespace lox
