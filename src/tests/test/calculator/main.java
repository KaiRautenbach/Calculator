package calculator;

//import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for the expression parser and the public main.evaluate() method.
 * Lives in the same package as main so it can also reach the package-private Parser.
 */
class CalculatorTest {

    private static String eval(String expression) {
        return main.evaluate(expression);
    }

    private static IllegalArgumentException assertBadInput(String expression) {
        return assertThrows(IllegalArgumentException.class, () -> eval(expression),
                "Expected an IllegalArgumentException for: " + expression);
    }

    // ------------------------------------------------------------------
    // Basic arithmetic
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Basic arithmetic")
    class BasicArithmetic {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1+1,        2",
                "5-3,        2",
                "3-5,        -2",
                "4*5,        20",
                "20/4,       5",
                "7/2,        3.5",
                "0+0,        0",
                "0*100,      0",
                "0/5,        0",
                "42,         42",
                "0,          0",
                "1000000*1000000, 1000000000000"
        })
        void singleOperations(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1+2+3+4,     10",
                "10-5-2,      3",
                "2*3*4,       24",
                "100/10/2,    5",
                "100/10*2,    20",
                "10-2+3,      11",
                "2*3/6,       1"
        })
        @DisplayName("Chains are evaluated left to right")
        void leftAssociativity(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }
    }

    // ------------------------------------------------------------------
    // Precedence and parentheses
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Precedence and parentheses")
    class PrecedenceAndParentheses {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "2+3*4,           14",
                "2*3+4,           10",
                "2+3*4-5/5,       13",
                "2*3+4*5,         26",
                "10-2*3,          4",
                "10/2+3,          8",
                "1+2*3-4/2,       5"
        })
        void multiplicationAndDivisionBindTighter(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "(2+3)*4,         20",
                "2*(3+4),         14",
                "(1+2)*(3+4),     21",
                "(10-4)/(1+2),    2",
                "((2+3)*(4-1)),   15",
                "2+3*(4-1),       11",
                "(2),             2",
                "((((1)))),       1",
                "(((2+3))*2),     10",
                "((1+2)*3)+((4-1)*2), 15"
        })
        void parenthesesOverridePrecedence(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @Test
        void deeplyNestedParentheses() {
            String expr = "(".repeat(200) + "7" + ")".repeat(200);
            assertEquals("7", eval(expr));
        }
    }

    // ------------------------------------------------------------------
    // Unary minus
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Unary minus")
    class UnaryMinus {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "-5,          -5",
                "-5+2,        -3",
                "-5-2,        -7",
                "2--3,        5",
                "2+-3,        -1",
                "2*-3,        -6",
                "-2*-3,       6",
                "--5,         5",
                "---5,        -5",
                "-(3+4),      -7",
                "-(2+3)*2,    -10",
                "2*-(1+1),    -4",
                "-(-(2)),     2",
                "10/-2,       -5",
                "-10/-2,      5"
        })
        void unaryMinusCases(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @Test
        void negativeZeroPrintsAsZero() {
            assertEquals("0", eval("-0"));
            assertEquals("0", eval("-0/5"));
            assertEquals("0", eval("0*-1"));
        }
    }

    // ------------------------------------------------------------------
    // Decimals and formatting
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Decimals and output formatting")
    class DecimalsAndFormatting {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1.5*4,        6",
                "2.5*2,        5",
                "0.5+0.25,     0.75",
                "10/4,         2.5",
                "1/8,          0.125",
                "3.14*2,       6.28",
                ".5+.5,        1",
                "5.,           5",
                "007,          7",
                "007.50,       7.5"
        })
        void decimalInputs(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "0.1+0.2,      0.3",
                "0.1*3,        0.3",
                "1.1+2.2,      3.3",
                "0.7+0.1,      0.8"
        })
        @DisplayName("Floating-point noise is cleaned up")
        void floatingPointNoiseIsRemoved(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1/3,          0.3333333333",
                "2/3,          0.6666666667",
                "-1/3,         -0.3333333333",
                "1/7,          0.1428571429"
        })
        @DisplayName("Results are rounded to 10 decimal places (HALF_UP)")
        void roundsToTenPlaces(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @Test
        void wholeNumbersHaveNoDecimalPoint() {
            assertEquals("5", eval("2.5*2"));
            assertEquals("100", eval("50*2"));
            assertEquals("-4", eval("-8/2"));
        }

        @Test
        void tinyValuesRoundToZero() {
            assertEquals("0", eval("0.00000000001"));
            assertEquals("0", eval("-0.00000000001"));
            assertEquals("0.0000000001", eval("0.0000000001"));
        }

        @Test
        void largeResultsUsePlainNotation() {
            assertEquals("1000000000000", eval("1000000*1000000"));
            assertEquals("100000000000000000000", eval("10000000000*10000000000"));
        }

        @Test
        void overflowGivesInfinity() {
            String big = "9".repeat(200);
            assertEquals("Infinity", eval(big + "*" + big));
            assertEquals("-Infinity", eval("-" + big + "*" + big));
        }

        @Test
        void infinityMinusInfinityGivesNaN() {
            String big = "9".repeat(200);
            String inf = "(" + big + "*" + big + ")";
            assertEquals("NaN", eval(inf + "-" + inf));
        }
    }

    // ------------------------------------------------------------------
    // Whitespace handling
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Whitespace")
    class Whitespace {

        @Test
        void spacesAreIgnored() {
            assertEquals("5", eval("2 + 3"));
            assertEquals("5", eval("  2  +  3  "));
            assertEquals("14", eval(" 2 + 3 * 4 "));
            assertEquals("20", eval("( 2 + 3 ) * 4"));
        }

        @Test
        void tabsAreIgnored() {
            assertEquals("5", eval("2\t+\t3"));
        }

        @Test
        void spaceAfterUnaryMinus() {
            assertEquals("-5", eval("- 5"));
            assertEquals("-7", eval("-  ( 3 + 4 )"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "    ", "\t"})
        void blankInputIsRejected(String blank) {
            assertBadInput(blank);
        }
    }

    // ------------------------------------------------------------------
    // Error handling
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Errors")
    class Errors {

        @ParameterizedTest(name = "{0} -> divide by zero")
        @ValueSource(strings = {"5/0", "0/0", "1/(2-2)", "5/0.0", "5/-0", "1/(1-1)*3", "2+4/0"})
        void divisionByZeroThrowsArithmeticException(String expression) {
            ArithmeticException ex = assertThrows(ArithmeticException.class, () -> eval(expression));
            assertEquals("Cannot divide by zero", ex.getMessage());
        }

        @Test
        void divisionByZeroIsNotAnIllegalArgumentException() {
            // The GUI and console catch both types; make sure they stay distinct.
            assertThrows(ArithmeticException.class, () -> eval("1/0"));
        }

        @Test
        void missingClosingParenthesis() {
            assertEquals("Missing closing parenthesis", assertBadInput("(2+3").getMessage());
            assertEquals("Missing closing parenthesis", assertBadInput("((2+3)").getMessage());
            assertEquals("Missing closing parenthesis", assertBadInput("2*(3+4").getMessage());
        }

        @Test
        void unexpectedClosingParenthesis() {
            assertEquals("Unexpected ')'", assertBadInput("2+3)").getMessage());
            assertEquals("Unexpected ')'", assertBadInput("(2+3))").getMessage());
        }

        @Test
        void unexpectedTrailingCharacter() {
            assertEquals("Unexpected '3'", assertBadInput("2 3").getMessage());
            assertEquals("Unexpected '('", assertBadInput("2(3)").getMessage());
            assertEquals("Unexpected 'e'", assertBadInput("1e5").getMessage());
        }

        @Test
        void missingOperandReportsPosition() {
            assertEquals("Expected a number at position 3", assertBadInput("2+").getMessage());
            assertEquals("Expected a number at position 1", assertBadInput("*5").getMessage());
            assertEquals("Expected a number at position 3", assertBadInput("2**3").getMessage());
            assertEquals("Expected a number at position 2", assertBadInput("()").getMessage());
            assertEquals("Expected a number at position 2", assertBadInput("-").getMessage());
        }

        @Test
        void positionCountsLeadingSpaces() {
            assertEquals("Expected a number at position 3", assertBadInput("  abc").getMessage());
        }

        @Test
        void emptyInputReportsPositionOne() {
            assertEquals("Expected a number at position 1", assertBadInput("").getMessage());
        }

        @ParameterizedTest(name = "\"{0}\" is rejected")
        @ValueSource(strings = {
                "abc", "2+a", "a+2", "2 x 3", "2^3", "5%2", "2,5", "2;3",
                "+5", "2++3", "2+*3", "2*/3", "/5", "2+3=", "$5", "2&3",
                "1.2.3", ".", "..", "2..5", "(", ")", "(()", "2+(", "(2+)", "()()"
        })
        void invalidExpressionsAreRejected(String expression) {
            assertBadInput(expression);
        }

        @Test
        void nullInputThrowsNullPointerException() {
            // Documents current behaviour: callers (GUI/console) always pass a string.
            assertThrows(NullPointerException.class, () -> eval(null));
        }
    }

    // ------------------------------------------------------------------
    // The Parser class directly
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Parser (raw double results)")
    class ParserDirect {

        private double parse(String expression) {
            return new main.Parser(expression).parse();
        }

        @Test
        void returnsRawDoubles() {
            assertEquals(5.0, parse("2+3"), 1e-12);
            assertEquals(0.30000000000000004, parse("0.1+0.2"), 0.0);
            assertEquals(-7.0, parse("-(3+4)"), 1e-12);
            assertEquals(2.5, parse("10/4"), 1e-12);
        }

        @Test
        void eachParserInstanceIsIndependent() {
            assertEquals(3.0, parse("1+2"), 1e-12);
            assertEquals(12.0, parse("3*4"), 1e-12);
            assertEquals(3.0, parse("1+2"), 1e-12);
        }

        @Test
        void throwsSameExceptionsAsEvaluate() {
            assertThrows(ArithmeticException.class, () -> parse("1/0"));
            assertThrows(IllegalArgumentException.class, () -> parse("2+"));
        }
    }

    // ------------------------------------------------------------------
    // Realistic longer expressions
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Longer mixed expressions")
    class LongerExpressions {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "2+3*(4-1),                          11",
                "(1+2)*(3+4)-5/(2+3),                20",
                "100-(2*(3+4))/(7-5)*3,              79",
                "((15/3)+(2*4))*(10-8),              26",
                "-(2+3)*(-4+1)/3,                    5",
                "1.5*(2+0.5)-0.25,                   3.5",
                "10/(2+3)*(4-1)+(8-6)*2,             10",
                "((((1+1)+1)+1)+1),                  5"
        })
        void mixedExpressions(String expression, String expected) {
            assertEquals(expected, eval(expression));
        }

        @Test
        void longSumChain() {
            StringBuilder sb = new StringBuilder("0");
            for (int i = 1; i <= 1000; i++) {
                sb.append('+').append(i);
            }
            assertEquals("500500", eval(sb.toString()));
        }

        @Test
        void repeatedCallsGiveConsistentResults() {
            for (int i = 0; i < 100; i++) {
                assertEquals("11", eval("2+3*(4-1)"));
            }
        }
    }
}