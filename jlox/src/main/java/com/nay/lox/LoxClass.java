package com.nay.lox;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
class LoxClass implements LoxCallable {
  final String name;
  private final Map<String, LoxFunction> methods;

  LoxClass(String name, Map<String, LoxFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  Optional<LoxFunction> findMethod(String name) {
    return Optional.ofNullable(methods.get(name));
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
