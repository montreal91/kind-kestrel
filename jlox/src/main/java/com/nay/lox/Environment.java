package com.nay.lox;

import java.util.HashMap;
import java.util.Map;


class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  void define(String name, Object value) {
    values.put(name, value);
  }

  Object get(Token name) {
    if (values.containsKey(name.getLexeme())) {
      return values.get(name.getLexeme());
    }

    if (enclosing != null) {
      return enclosing.get(name);
    }

    throw new RuntimeError(
        name,
        "Undefined variable '" + name.getLexeme() + "'."
    );
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.getLexeme())) {
      values.put(name.getLexeme(), value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }
    throw new RuntimeError(
        name,
        "Trying to assign value to undefined variable '"
            + name.getLexeme()
            + "'."
    );
  }

  Object getAt(Integer distance, String name) {
    return ancestor(distance).values.get(name);
  }

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.getLexeme(), value);
  }

  private Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      if (environment != null) {
        environment = environment.enclosing;
      } else {
        // Should throw Resolution error right here;
      }
    }
    return environment;
  }
}
