package com.nay.lox;

import java.util.HashMap;
import java.util.Map;


class LoxInstance {
  private final LoxClass loxClass;
  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass loxClass) {
    this.loxClass = loxClass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.getLexeme())) {
      return fields.get(name.getLexeme());
    }

    throw new RuntimeError(
        name,
        "Undefined property '" + name.getLexeme() + "'."
    );
  }

  void set(Token name, Object value) {
    fields.put(name.getLexeme(), value);
  }

  @Override
  public String toString() {
    return loxClass.name + " instance";
  }
}
