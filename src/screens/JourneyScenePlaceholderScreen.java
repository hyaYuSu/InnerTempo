package screens;

import config.GameConfig;
import manager.ScreenManager;
import model.Journey;
import model.JourneyScene;
import ui.GameUiFactory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class JourneyScenePlaceholderScreen {
    private final ScreenManager controller;
    private final Journey journey;
    private final JourneyScene scene;

    public JourneyScenePlaceholderScreen(ScreenManager controller, Journey journey, JourneyScene scene) {
        this.controller = controller;
        this.journey = journey;
        this.scene = scene;
    }

    public Scene create() {
        BorderPane root = new BorderPane();
        root.setStyle(backgroundStyle());

        VBox content = new VBox(22);
        content.setPadding(new Insets(60));
        content.setAlignment(Pos.TOP_LEFT);

        Text journeyTitle = new Text(journey.getTitle());
        journeyTitle.setFill(Color.GOLDENROD);
        journeyTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 42));

        Text sceneTitle = new Text("Scene " + scene.getNumber() + ": " + scene.getTitle());
        sceneTitle.setFill(Color.WHITE);
        sceneTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 30));

        Text background = new Text(journey.getBackgroundLabel());
        background.setFill(Color.LIGHTGRAY);
        background.setFont(Font.font("Georgia", 20));
        background.setWrappingWidth(760);

        Text story = new Text(scene.getStoryText().isBlank() ? scene.getSummary() : scene.getStoryText());
        story.setFill(Color.WHITE);
        story.setFont(Font.font("Georgia", 19));
        story.setWrappingWidth(780);

        Button backButton = GameUiFactory.createSmallButton("BACK");
        backButton.setOnAction(e -> controller.showJourneySelect(journey.getId()));

        content.getChildren().addAll(journeyTitle, sceneTitle, background, story, backButton);
        root.setCenter(content);

        return new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
    }

    private String backgroundStyle() {
        return switch (scene.getNumber()) {
            case 1 -> "-fx-background-color: linear-gradient(to bottom, #4b3527, #120d0a);";
            case 2 -> "-fx-background-color: linear-gradient(to bottom, #2b2526, #080707);";
            case 3 -> "-fx-background-color: linear-gradient(to bottom, #253548, #090d12);";
            case 4 -> "-fx-background-color: linear-gradient(to bottom, #463820, #0f0b08);";
            default -> "-fx-background-color: linear-gradient(to bottom, #5d3a2f, #1b1015);";
        };
    }
}
