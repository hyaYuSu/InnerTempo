package screens;

import config.GameConfig;
import content.StageResultStoryCatalog;
import manager.ScreenManager;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import ui.GameUiFactory;
import ui.JourneyTheme;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class StageResultStoryScreen {
    private final ScreenManager controller;
    private final PlayableStage playableStage;
    private final ScoreTracker scoreTracker;
    private final JourneyTheme theme;

    public StageResultStoryScreen(
            ScreenManager controller,
            PlayableStage playableStage,
            ScoreTracker scoreTracker
    ) {
        this.controller = controller;
        this.playableStage = playableStage;
        this.scoreTracker = scoreTracker;
        this.theme = JourneyTheme.forStage(playableStage);
    }

    public Scene create() {
        BorderPane root = new BorderPane();
        root.setStyle(theme.getGameplayBackgroundStyle());

        VBox content = new VBox(22);
        content.setPadding(new Insets(70));
        content.setAlignment(Pos.CENTER_LEFT);

        Text journeyLabel = new Text("Scene " + playableStage.getNumber());
        journeyLabel.setFill(theme.getSoftTextColor());
        journeyLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 20));

        Text title = new Text(StageResultStoryCatalog.titleFor(playableStage, scoreTracker));
        title.setFill(theme.getAccentColor());
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 42));

        Text story = new Text(StageResultStoryCatalog.storyFor(playableStage, scoreTracker));
        story.setFill(Color.WHITE);
        story.setFont(Font.font("Georgia", 23));
        story.setWrappingWidth(820);

        Text prompt = new Text("Press SPACE to view results");
        prompt.setFill(Color.LIGHTGRAY);
        prompt.setFont(Font.font("Georgia", FontWeight.BOLD, 15));

        Button continueButton = GameUiFactory.createSmallButton("RESULTS");
        continueButton.setOnAction(e -> controller.showResults(playableStage, scoreTracker));

        content.getChildren().addAll(journeyLabel, title, story, prompt, continueButton);
        root.setCenter(content);

        Scene scene = new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                controller.showResults(playableStage, scoreTracker);
            }
        });

        return scene;
    }
}
