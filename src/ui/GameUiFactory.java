package ui;

import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public final class GameUiFactory {
    private GameUiFactory() {
    }

    public static Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent;"
                        + "-fx-underline: true;"
        );
        addGoldHover(button);
        return button;
    }

    public static Button createGameBoard(String text) {
        return createMenuButton(text);
    }

    public static Button createStageBoard(String text, boolean unlocked) {
        Button button = new Button(text);
        button.setPrefSize(500, 65);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setFont(Font.font("Georgia", FontWeight.BOLD, 24));

        if (unlocked) {
            button.setTextFill(Color.WHITE);
            button.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-border-color: white;"
                            + "-fx-border-width: 0 0 2 0;"
            );
            addGoldHover(button);
        } else {
            button.setTextFill(Color.GRAY);
            button.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-border-color: #555555;"
                            + "-fx-border-width: 0 0 2 0;"
            );
            addLockedHover(button);
        }

        return button;
    }

    public static Button createSmallButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(120, 40);
        button.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent;"
                        + "-fx-underline: true;"
        );
        addGoldHover(button);
        return button;
    }

    public static Text createOptionValueText() {
        Text text = new Text();
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        return text;
    }

    public static Text createResultText(String value) {
        Text text = new Text(value);
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        return text;
    }

    private static void addGoldHover(Button button) {
        button.setOnMouseEntered(e -> {
            if (!button.isDisabled()) {
                button.setTextFill(Color.GOLDENROD);
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisabled()) {
                button.setTextFill(Color.WHITE);
            }
        });
    }

    private static void addLockedHover(Button button) {
        button.setOnMouseEntered(e -> button.setTextFill(Color.GOLDENROD));
        button.setOnMouseExited(e -> button.setTextFill(Color.GRAY));
    }
}
