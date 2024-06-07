# Middleware

The purpose of middleware is to consume the AST produced by the compiler frontend
and transform it into the intermediate representation consumed by the code generator.
Some checks and optimizations are also possible, such as function inlining.
