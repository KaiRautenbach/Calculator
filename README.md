# Calculator

A calculator written in Java with a JavaFX window and a text-based console mode. Both share the same hand-written expression parser, so `2+3*(4-1)` gives the same answer in either one.

## Features

- Evaluates full expressions, not just two numbers at a time
- Correct operator precedence (`2+3*4` is `14`)
- Parentheses and nesting: `(2+3)*(4-1)`
- Negative numbers and unary minus: `-5+2`, `-(3+4)`
- Decimal numbers: `1.5*4`
- Clean output: `5` instead of `5.0`, and `0.1+0.2` gives `0.3`
- Friendly error messages (divide by zero, missing parenthesis, unexpected characters)
- GUI with a button pad, plus keyboard input (press Enter to evaluate)
- Console mode for quick use from a terminal

## Requirements

- **JDK 17 or newer** (the code uses `switch` arrows, which need 14+)
- **Maven 3.6+** (or IntelliJ IDEA, which bundles Maven)
- Internet access the first time, so Maven can download JavaFX

JavaFX is not included in the JDK. The `pom.xml` pulls it in as a dependency (`org.openjfx:javafx-controls`), so no manual install is needed.

## Project structure

```
calculator/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── calculator/
                ├── main.java            # Parser, evaluate(), console mode, entry point
                ├── CalculatorApp.java   # JavaFX window
                └── Launcher.java        # Entry point that avoids the JavaFX module-path error
```

| File | Purpose |
|------|---------|
| `main.java` | Contains the recursive-descent `Parser`, the public `evaluate(String)` method used by both interfaces, and the console loop. |
| `CalculatorApp.java` | The JavaFX `Application`: display field, error label, and button grid. Calls `main.evaluate()`. |
| `Launcher.java` | A plain class (it does not extend `Application`) that calls `main.main(args)`. Running this from an IDE avoids the "JavaFX runtime components are missing" error. |

## Running the app

### With Maven (terminal)

From the folder containing `pom.xml`:

```
mvn clean javafx:run
```

To run the console version instead:

```
mvn javafx:run -Djavafx.args="--console"
```

### With IntelliJ IDEA

1. Open the project folder (**File → Open**, select `pom.xml`, choose **Open as Project**).
2. Click **Reload All Maven Projects** in the Maven tool window and wait for the JavaFX libraries to appear under External Libraries.
3. Open `Launcher.java` and click the green arrow beside `main`, then choose **Run 'Launcher.main()'**.

> **Run `Launcher`, not `CalculatorApp`.** Starting a class that extends `Application` directly makes Java look for JavaFX on the module path, which fails with *"JavaFX runtime components are missing"*. `Launcher` avoids this.

To use console mode from IntelliJ, edit the run configuration and add `--console` under **Program arguments**.

## Using the calculator

### GUI

- Click the buttons, or type into the display field with your keyboard.
- Press **Enter** or **=** to calculate.
- **C** clears everything, **DEL** removes the last character.
- After a result appears, typing a number starts a new calculation, while typing an operator continues from the result.
- Errors appear in red under the display.

### Console

```
Enter an expression like 2+3*(4-1). Type 'exit' to quit.
> 2+3*(4-1)
Result: 11

> 10/0
Error: Cannot divide by zero

> exit
```

## Supported syntax

| Feature | Example | Result |
|---------|---------|--------|
| Add, subtract | `10-4+2` | `8` |
| Multiply, divide | `6*7/2` | `21` |
| Precedence | `2+3*4` | `14` |
| Parentheses | `(2+3)*4` | `20` |
| Unary minus | `-(3+4)` | `-7` |
| Decimals | `1.5*4` | `6` |
| Spaces | `2 + 3` | `5` |

## How it works

The parser reads the input one character at a time using **recursive descent**. Each grammar rule is a method, and the rules are layered so that precedence falls out naturally:

```
expression = term   { ("+" | "-") term }
term       = factor { ("*" | "/") factor }
factor     = number | "(" expression ")" | "-" factor
```

- `parseExpression()` handles `+` and `-` (lowest precedence).
- `parseTerm()` handles `*` and `/`, which bind tighter.
- `parseFactor()` handles numbers, parenthesised sub-expressions, and unary minus.

`main.evaluate(String)` runs the parser and formats the result with `BigDecimal` to remove floating-point noise. It throws `IllegalArgumentException` or `ArithmeticException` on bad input, which the console and the GUI each catch and display in their own way.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Error: JavaFX runtime components are missing` | Run `calculator.Launcher` (or use `mvn javafx:run`), not `CalculatorApp`. |
| JavaFX libraries missing under External Libraries | Right-click `pom.xml` → **Add as Maven Project**, then reload Maven. |
| `invalid target release` or `switch` syntax errors | Your JDK is too old. Use JDK 17+, or change `maven.compiler.release` in `pom.xml`. |
| Window doesn't open from Maven | Make sure you ran `javafx:run`, not `exec:java` or a plain `java -jar`. |
| Odd error text for input like `1.2.3` | Malformed numbers are reported with Java's own parse message. See the ideas below. |

## Limitations and ideas for next steps

- No exponents, percentages, square roots, or functions like `sin`
- No calculation history
- Malformed numbers such as `1.2.3` produce a generic error message
- Class `main` breaks Java's naming convention. Consider renaming it to `Calculator` (update `Launcher`, `CalculatorApp`, and `<main.class>` in `pom.xml` to match)
- Possible additions: `^` for powers, a history panel, keyboard shortcuts for `C` and `DEL`, unit tests for the parser with JUnit

## License

Add a license of your choice here (MIT is a common default for personal projects).