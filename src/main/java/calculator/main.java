package calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

import javafx.application.Application;

public class main {

    // Parser that walks through the string one character at a time
    static class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) {
            this.input = input;
        }

        double parse() {
            double result = parseExpression();
            skipSpaces();
            if (pos < input.length()) {
                throw new IllegalArgumentException("Unexpected '" + input.charAt(pos) + "'");
            }
            return result;
        }

        // expression = term { (+|-) term }
        private double parseExpression() {
            double value = parseTerm();
            while (true) {
                skipSpaces();
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        // term = factor { (*|/) factor }
        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                skipSpaces();
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    double divisor = parseFactor();
                    if (divisor == 0) {
                        throw new ArithmeticException("Cannot divide by zero");
                    }
                    value /= divisor;
                } else {
                    return value;
                }
            }
        }

        // factor = number | ( expression ) | -factor
        private double parseFactor() {
            skipSpaces();

            if (match('-')) {
                return -parseFactor();
            }

            if (match('(')) {
                double value = parseExpression();
                skipSpaces();
                if (!match(')')) {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                }
                return value;
            }

            return parseNumber();
        }

        private double parseNumber() {
            skipSpaces();
            int start = pos;
            while (pos < input.length()
                    && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            if (start == pos) {
                throw new IllegalArgumentException("Expected a number at position " + (start + 1));
            }
            return Double.parseDouble(input.substring(start, pos));
        }

        // If the next character is c, consume it and return true
        private boolean match(char c) {
            if (pos < input.length() && input.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        private void skipSpaces() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }
    }

    // Used by both the console and the GUI.
    // Throws ArithmeticException / IllegalArgumentException on bad input.
    public static String evaluate(String expression) {
        double result = new Parser(expression).parse();
        return format(result);
    }

    // 5.0 -> "5", 0.1+0.2 -> "0.3"
    private static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        return BigDecimal.valueOf(value)
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter an expression like 2+3*(4-1). Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("exit")) break;
            if (line.isEmpty()) continue;

            try {
                System.out.println("Result: " + evaluate(line) + "\n");
            } catch (ArithmeticException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }

        scanner.close();
    }

    public static void main(String[] args) {
        // Run with the argument --console to keep using the text version
        if (args.length > 0 && args[0].equals("--console")) {
            runConsole();
        } else {
            Application.launch(CalculatorApp.class, args);
        }
    }
}