# Rudny Lox Compiler (RLC)

**Rudny** (Russian: Рудный) is a small city located in Qostanay Region in the north of Kazakhstan.
Its name means "The City of (Iron) Ore" and it was built to develop an iron ore mining facility.
There's nothing particularly remarkable about this city,
but it holds a special place in my heart because it is my birthplace.

My Lox Compiler compiles to JVM bytecode.
It's not objectively special, but it's my first JVM backend compiler, so it's special to me.

## Features (16/36)
The brief list of features I want to implement

### Meta
- [x] The compiler should be a single executable file
- [x] The result of successful compilation should be a single executable jar file
- [ ] Run Lox testsuite for RLC
- [ ] Set up CI/CD pipeline to run tests and testsuite before merge

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
- [ ] Assignment

### Statements
- [x] Expr Statement
- [x] Print Statement
- [x] Block
- [x] Var Statement
- [x] Lexical Scopes
- [ ] If Statement
- [ ] While Statement
- [ ] For Statement

### Functions
- [ ] Function Declarations
- [ ] Function Calls
- [ ] Closures

### Classes
- [ ] Class Declarations
- [ ] Creating Instances
- [ ] Properties on Instances
- [ ] Methods on Classes
- [ ] This
- [ ] Constructors and Initializers
- [ ] Inheritance
- [ ] Super

### Optimizations
- [ ] Calculation of Constant Expressions
- [ ] Data Unwrapping
- [ ] NilChecker

Maybe there will be more optimizations
