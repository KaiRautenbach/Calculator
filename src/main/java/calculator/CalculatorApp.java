package calculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CalculatorApp extends Application {

    private static final String MULTIPLY = "\u00D7"; // ×
    private static final String DIVIDE = "\u00F7";   // ÷

    private static final String[][] BUTTONS = {
            {"C", "(", ")", "DEL"},
            {"7", "8", "9", DIVIDE},
            {"4", "5", "6", MULTIPLY},
            {"1", "2", "3", "-"},
            {"0", ".", "=", "+"}
    };

    private TextField display;
    private Label message;
    private boolean justEvaluated = false;

    @Override
    public void start(Stage stage) {
        display = new TextField();
        display.setPromptText("Type or tap an expression");
        display.setStyle("-fx-font-size: 24px;");
        display.setAlignment(Pos.CENTER_RIGHT);
        // Pressing Enter in the text field evaluates it
        display.setOnAction(e -> evaluate());

        message = new Label(" ");
        message.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 13px;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        for (int row = 0; row < BUTTONS.length; row++) {
            for (int col = 0; col < BUTTONS[row].length; col++) {
                String label = BUTTONS[row][col];
                Button button = new Button(label);
                button.setMaxWidth(Double.MAX_VALUE);
                button.setMaxHeight(Double.MAX_VALUE);
                button.setMinSize(60, 50);
                button.setStyle("-fx-font-size: 18px;");
                button.setFocusTraversable(false);
                button.setOnAction(e -> onButton(label));
                GridPane.setHgrow(button, Priority.ALWAYS);
                GridPane.setVgrow(button, Priority.ALWAYS);
                grid.add(button, col, row);
            }
        }

        VBox root = new VBox(10, display, message, grid);
        root.setPadding(new Insets(15));
        VBox.setVgrow(grid, Priority.ALWAYS);

        stage.setTitle("Calculator");
        stage.setScene(new Scene(root, 320, 420));
        stage.setMinWidth(300);
        stage.setMinHeight(400);
        stage.show();
        display.requestFocus();
    }

    private void onButton(String label) {
        message.setText(" ");

        switch (label) {
            case "C" -> display.clear();
            case "DEL" -> {
                String text = display.getText();
                if (!text.isEmpty()) {
                    display.setText(text.substring(0, text.length() - 1));
                }
            }
            case "=" -> {
                evaluate();
                return;
            }
            default -> {
                boolean isOperator = label.equals("+") || label.equals("-")
                        || label.equals(MULTIPLY) || label.equals(DIVIDE);

                // Typing a digit after a result starts a new calculation;
                // typing an operator continues from the result.
                if (justEvaluated && !isOperator) {
                    display.clear();
                }
                display.appendText(toParserSymbol(label));
            }
        }

        justEvaluated = false;
        display.requestFocus();
        display.positionCaret(display.getText().length());
    }

    private void evaluate() {
        String expression = display.getText().trim();
        if (expression.isEmpty()) {
            return;
        }

        try {
            String result = main.evaluate(expression);
            display.setText(result);
            message.setText(" ");
            justEvaluated = true;
        } catch (ArithmeticException | IllegalArgumentException e) {
            message.setText(e.getMessage());
        }

        display.requestFocus();
        display.positionCaret(display.getText().length());
    }

    // The parser understands * and /, so translate the pretty symbols
    private String toParserSymbol(String label) {
        if (label.equals(MULTIPLY)) return "*";
        if (label.equals(DIVIDE)) return "/";
        return label;
    }
}