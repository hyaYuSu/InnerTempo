package screens;

import config.GameConfig;
import manager.ScreenManager;
import ui.GameUiFactory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MainMenuScreen {
    private final ScreenManager controller;

    public MainMenuScreen(ScreenManager controller) {
        this.controller = controller;
    }

    public Scene create() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #050505;");

        Text title = new Text("InnerTempo");
        title.setFill(Color.GOLDENROD);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 42));

        Button playButton = GameUiFactory.createMenuButton("PLAY");
        Button optionsButton = GameUiFactory.createMenuButton("OPTIONS");

        playButton.setOnAction(e -> controller.showJourneySelect());
        optionsButton.setOnAction(e -> controller.showOptions());

        root.getChildren().addAll(title, playButton, optionsButton);

        return new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
    }
}
