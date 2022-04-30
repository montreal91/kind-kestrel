package com.nay.lox;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
class LoxClass implements LoxCallable {
  final String name;
  final LoxClass superclass;
  private final Map<String, LoxFunction> methods;

  LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
    this.name = name;
    this.superclass = superclass;
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
    LoxInstance instance = new LoxInstance(this);
    Optional<LoxFunction> initializer = findMethod("init");

    initializer.ifPresent(
        init -> init.bind(instance).call(interpreter, arguments)
    );

    return instance;
  }

  @Override
  public int arity() {
    Optional<LoxFunction> initializer = findMethod("init");
    return initializer.map(LoxFunction::arity)
                      .orElse(0);
  }
}
