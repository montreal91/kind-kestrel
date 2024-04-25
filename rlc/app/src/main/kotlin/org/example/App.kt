
package org.example

class App {
  val greeting: String
    get() {
      return "Hello JVM!"
    }
}

fun main() {
  println(App().greeting)
}
