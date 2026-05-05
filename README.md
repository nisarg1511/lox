# Jlox

A tree-walk interpreter for the Lox programming language, implemented in Java while working through Robert Nystrom's [Crafting Interpreters](https://craftinginterpreters.com/).

The project covers the complete Java interpreter track from the book and includes a few extra language features such as `break`, `continue`, the ternary operator, the comma operator, static methods, and more detailed source-location error reporting.

## Why I Built This

I built this project to understand how programming languages work below the surface: how source code becomes tokens, how tokens become an abstract syntax tree, how lexical scope is resolved, and how an interpreter evaluates programs at runtime.

This is not a toy syntax highlighter or parser-only project. It includes the full interpreter pipeline:

- Lexical scanner
- Recursive descent parser
- AST representation generated with a small Java tool
- Resolver for lexical scope analysis
- Tree-walk interpreter
- Runtime environments for variables and closures
- Function, class, instance, inheritance, and method dispatch support
- User-facing syntax and runtime error messages

## Features

### Language Basics

- Numbers, strings, booleans, and `nil`
- Arithmetic and comparison operators
- Logical operators: `and`, `or`, `!`
- Variable declarations and assignment
- Blocks and lexical scope
- `if` / `else`
- `while` loops
- `for` loops desugared into `while`
- `break` and `continue`
- `print` statements

### Expressions

- Unary and binary expressions
- Grouping with parentheses
- Function calls
- Property access and assignment
- Ternary expressions: `condition ? valueA : valueB`
- Comma expressions

### Functions

- Function declarations
- First-class functions
- Closures
- Return statements
- Arity checking
- Native `clock()` function

### Object-Oriented Features

- Classes and instances
- Fields and methods
- Initializers with `init`
- `this`
- Inheritance
- `super`
- Static methods

### Developer Experience

- Script execution from a file
- Interactive REPL
- Syntax error reporting with line and column information
- Runtime error reporting with source-location context

## Project Structure

```text
com/interpreter/lox/
  Lox.java            # CLI entry point and REPL
  Scanner.java        # Lexical scanner
  Parser.java         # Recursive descent parser
  Expr.java           # Expression AST nodes
  Stmt.java           # Statement AST nodes
  Resolver.java       # Static scope resolver
  Interpreter.java    # Tree-walk interpreter
  Environment.java    # Runtime variable environments
  LoxFunction.java    # Function and closure implementation
  LoxClass.java       # Class representation
  LoxInstance.java    # Instance fields and method lookup

com/interpreter/tool/
  GenerateAst.java    # Utility used to generate AST classes
```

## Requirements

- JDK 21 or newer

The interpreter uses modern Java syntax, including pattern matching in `switch`, so an older JDK may fail to compile.

## Running

Compile the interpreter:

```bash
javac com/interpreter/lox/*.java com/interpreter/tool/*.java
```

Start the REPL:

```bash
java com.interpreter.lox.Lox
```

Run a Lox file:

```bash
java com.interpreter.lox.Lox path/to/program.lox
```

## Example

Create a file named `example.lox`:

```lox
class Greeter {
  init(name) {
    this.name = name;
  }

  greet() {
    print "Hello, " + this.name + "!";
  }
}

fun makeCounter() {
  var count = 0;

  fun increment() {
    count = count + 1;
    return count;
  }

  return increment;
}

var greeter = Greeter("Nisarg");
greeter.greet();

var counter = makeCounter();
print counter();
print counter();
print true ? "ternary works" : "unreachable";
```

Run it:

```bash
java com.interpreter.lox.Lox example.lox
```

Expected output:

```text
Hello, Nisarg!
1
2
ternary works
```

## What I Learned

- How interpreters split source execution into scanning, parsing, resolving, and evaluation phases
- How recursive descent parsers encode grammar rules directly in code
- How the visitor pattern helps evaluate AST nodes cleanly
- How lexical environments enable closures
- How classes, instances, `this`, and `super` can be modeled at runtime
- How syntax and runtime errors can be reported with useful source context

## Attribution

This project follows the Java interpreter implementation from [Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom. The implementation is written as a learning project, with additional language features and error-reporting improvements added while studying the interpreter design.
