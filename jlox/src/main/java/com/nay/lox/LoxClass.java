package com.nay.lox;


import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
class LoxClass implements LoxCallable {
  final String name;

  LoxClass(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "<class '" + name + "'>";
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    return new LoxInstance(this);
  }

  @Override
  public int arity() {
    return 0;
  }
}
