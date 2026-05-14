package screens;

import config.GameConfig;
import manager.ScreenManager;
import settings.GameplaySettings;
import ui.GameUiFactory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class OptionsScreen {
    private final ScreenManager controller;
    private final GameplaySettings options;

    public OptionsScreen(ScreenManager controller, GameplaySettings options) {
        this.controller = controller;
        this.options = options;
    }

    public Scene create() {
        VBox root = new VBox(26);
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #050505;");

        Text title = new Text("OPTIONS");
        title.setFill(Color.GOLDENROD);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 42));

        Text speedValue = GameUiFactory.createOptionValueText();
        Text timingValue = GameUiFactory.createOptionValueText();
        Text offsetValue = GameUiFactory.createOptionValueText();

        Runnable refresh = () -> {
            speedValue.setText(String.format("Note Speed: %.1fx", options.getNoteSpeedMultiplier()));
            timingValue.setText("Timing: " + options.timingLabel());
            offsetValue.setText(String.format("Input Offset: %+d ms", Math.round(options.getInputOffsetSeconds() * 1000)));
        };

        Button speedDown = GameUiFactory.createSmallButton("-");
        Button speedUp = GameUiFactory.createSmallButton("+");
        speedDown.setOnAction(e -> {
            options.decreaseNoteSpeed();
            refresh.run();
        });
        speedUp.setOnAction(e -> {
            options.increaseNoteSpeed();
            refresh.run();
        });

        Button strictButton = GameUiFactory.createSmallButton("STRICT");
        Button normalButton = GameUiFactory.createSmallButton("NORMAL");
        Button relaxedButton = GameUiFactory.createSmallButton("RELAXED");
        strictButton.setOnAction(e -> {
            options.useStrictTiming();
            refresh.run();
        });
        normalButton.setOnAction(e -> {
            options.useNormalTiming();
            refresh.run();
        });
        relaxedButton.setOnAction(e -> {
            options.useRelaxedTiming();
            refresh.run();
        });

        Button offsetDown = GameUiFactory.createSmallButton("-10");
        Button offsetReset = GameUiFactory.createSmallButton("RESET");
        Button offsetUp = GameUiFactory.createSmallButton("+10");
        offsetDown.setOnAction(e -> {
            options.adjustInputOffsetMillis(-10);
            refresh.run();
        });
        offsetReset.setOnAction(e -> {
            options.resetInputOffset();
            refresh.run();
        });
        offsetUp.setOnAction(e -> {
            options.adjustInputOffsetMillis(10);
            refresh.run();
        });

        Button backButton = GameUiFactory.createSmallButton("BACK");
        backButton.setOnAction(e -> controller.showMainMenu());

        refresh.run();

        root.getChildren().addAll(
                title,
                speedValue,
                new HBox(14, speedDown, speedUp),
                timingValue,
                new HBox(14, strictButton, normalButton, relaxedButton),
                offsetValue,
                new HBox(14, offsetDown, offsetReset, offsetUp),
                backButton
        );

        for (Node node : root.getChildren()) {
            if (node instanceof HBox row) {
                row.setAlignment(Pos.CENTER_LEFT);
            }
        }

        return new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
    }
}
