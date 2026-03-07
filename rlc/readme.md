# Rudny Lox Compiler (RLC)

**Rudny** (Russian: Рудный) is a small city located in Qostanay Region in the north of Kazakhstan.
Its name means "The City of (Iron) Ore" and it was built to develop an iron ore mining facility.
There's nothing particularly remarkable about this city,
but it holds a special place in my heart because it is my birthplace.

My Lox Compiler compiles to JVM bytecode.
It's not objectively special, but it's my first JVM backend compiler, so it's special to me.

## Features (29/29)
The brief list of features I want to implement

### Data Types
- [x] Booleans
- [x] Numbers
- [x] Strings
- [x] Nil

### Expressions
- [x] Arithmetic
- [x] Comparison and Equality
- [x] Logical Operators
- [x] Precedence
- [x] Grouping
- [x] Assignment

### Statements
- [x] Expr Statement
- [x] Print Statement
- [x] Block
- [x] Var Statement
- [x] Lexical Scopes
- [x] If Statement
- [x] While Statement
- [x] For Statement

### Functions
- [x] Function Declarations
- [x] Function Calls
- [x] Closures

### Classes
- [x] Class Declarations
- [x] Creating Instances
- [x] Properties on Instances
- [x] Methods on Classes
- [x] This
- [x] Constructors and Initializers
- [x] Inheritance
- [x] Super

## Extra (2 / 6)

### Meta
- [x] The compiler should be a single executable file
- [x] The result of a successful compilation should be a single executable jar file

### Optimizations
- [ ] Calculation of Constant Expressions
- [ ] Data Unwrapping
- [ ] NilChecker
- [ ] Call-site specialization

Maybe there will be more optimizations
