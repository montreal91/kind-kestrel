package com.nay.lox;

import java.util.HashMap;
import java.util.Map;


class Environment {
  private final Map<String, Object> values = new HashMap<>();

  void define(String name, Object value) {
    values.put(name, value);
  }

  Object get(Token name) {
    if (values.containsKey(name.getLexeme())) {
      return values.get(name.getLexeme());
    }

    throw new RuntimeError(
        name,
        "Undefined variable '" + name.getLexeme() + "'."
    );
  }

  public void assign(Token name, Object value) {
    if (values.containsKey(name.getLexeme())) {
      values.put(name.getLexeme(), value);
      return;
    }
    throw new RuntimeError(
        name,
        "Undefined variable '" + name.getLexeme() + "'."
    );
  }
}
