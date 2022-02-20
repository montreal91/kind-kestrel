package com.nay.lox;

@SuppressWarnings("ClassCanBeRecord")
class LoxInstance {
  private final LoxClass loxClass;

  LoxInstance(LoxClass loxClass) {
    this.loxClass = loxClass;
  }

  @Override
  public String toString() {
    return loxClass.name + " instance";
  }
}
