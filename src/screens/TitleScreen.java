package screens;

import config.GameConfig;
import manager.ScreenManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TitleScreen {
    private final ScreenManager controller;

    public TitleScreen(ScreenManager controller) {
        this.controller = controller;
    }

    public Scene create() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #050505;");

        Text title = new Text("INNER TEMPO");
        title.setFill(Color.GOLDENROD);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 58));

        Text backgroundText = new Text("Background");
        backgroundText.setFill(Color.GRAY);
        backgroundText.setFont(Font.font("Georgia", 24));

        Text pressText = new Text("Press SPACE to start...");
        pressText.setFill(Color.LIGHTGRAY);
        pressText.setFont(Font.font("Georgia", 18));

        VBox center = new VBox(120, title, backgroundText);
        center.setAlignment(Pos.TOP_LEFT);
        center.setPadding(new Insets(40));

        root.setCenter(center);
        root.setBottom(pressText);
        BorderPane.setAlignment(pressText, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(pressText, new Insets(20));

        Scene scene = new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                controller.showMainMenu();
            }
        });

        return scene;
    }
}
