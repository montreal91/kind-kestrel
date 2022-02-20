package com.nay.lox;


@SuppressWarnings("ClassCanBeRecord")
class LoxClass {
  final String name;

  LoxClass(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "<class '" + name + "'>";
  }
}
