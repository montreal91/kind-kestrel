const std = @import("std");

pub fn build(b: *std.build.Builder) void {
  const target = b.standardTargetOptions(.{});
  const mode = b.standardReleaseOptions();

  const der_lox = b.addExecutable("DerLox", null);
  der_lox.setTarget(target);
  der_lox.setBuildMode(mode);
  der_lox.install();
  der_lox.linkLibC();
  der_lox.linkLibCpp();
  der_lox.addCSourceFiles(&.{
    "src/Main.cc", // Main

    // Othar files in alphabetical order
    "src/Common.cc",
    "src/Compiler.cc",
    "src/Interpreter.cc",
    "src/Lexer.cc",
    "src/Parser.cc",
    "src/Token.cc",
    "src/Vm.cc",
  }, &.{
    "-std=c++17", "-Wall"
  });
}
