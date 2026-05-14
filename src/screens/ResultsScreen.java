package screens;

import config.GameConfig;
import manager.SaveManager;
import manager.ScreenManager;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import ui.GameUiFactory;
import ui.JourneyTheme;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ResultsScreen {
    private final ScreenManager controller;
    private final SaveManager saveManager;
    private final PlayableStage playableStage;
    private final ScoreTracker scoreTracker;
    private final JourneyTheme theme;

    public ResultsScreen(
            ScreenManager controller,
            SaveManager saveManager,
            PlayableStage playableStage,
            ScoreTracker scoreTracker
    ) {
        this.controller = controller;
        this.saveManager = saveManager;
        this.playableStage = playableStage;
        this.scoreTracker = scoreTracker;
        this.theme = JourneyTheme.forStage(playableStage);
    }

    public Scene create() {
        boolean cleared = scoreTracker.getAccuracy() >= GameConfig.CLEAR_ACCURACY;
        int previousBest = saveManager.getHighScore(playableStage);
        boolean newBest = scoreTracker.getScore() > previousBest;
        boolean unlockedNext = saveManager.recordResult(playableStage, scoreTracker);

        VBox root = new VBox(17);
        root.setAlignment(Pos.CENTER);
        root.setStyle(theme.getGameplayBackgroundStyle());

        Text title = new Text(cleared ? "STAGE CLEARED" : "STAGE FAILED");
        title.setFill(cleared ? theme.getAccentColor() : Color.CRIMSON);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 46));

        Text gradeText = new Text("Grade " + scoreTracker.getGrade());
        gradeText.setFill(gradeColor());
        gradeText.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        Text newBestLine = GameUiFactory.createResultText(newBest ? "New Best!" : "Best: " + saveManager.getHighScore(playableStage));
        Text scoreLine = GameUiFactory.createResultText("Score: " + scoreTracker.getScore());
        Text bonusLine = GameUiFactory.createResultText("Bonus: " + scoreTracker.getBonusScore());
        Text goldLine = GameUiFactory.createResultText(
                "Gold Notes: " + scoreTracker.getGoldHitCount()
                        + " hit / " + scoreTracker.getGoldMissCount()
                        + " missed"
        );
        Text accuracyLine = GameUiFactory.createResultText(String.format("Accuracy: %.1f%%", scoreTracker.getAccuracy()));
        Text timingLine = GameUiFactory.createResultText(String.format(
                "Timing: %.0fms avg error / %+dms bias",
                scoreTracker.getAverageAbsoluteTimingOffsetMillis(),
                Math.round(scoreTracker.getAverageSignedTimingOffsetMillis())
        ));
        Text timingBalanceLine = GameUiFactory.createResultText(
                "Early " + scoreTracker.getEarlyHitCount()
                        + " / Late " + scoreTracker.getLateHitCount()
        );
        Text comboLine = GameUiFactory.createResultText("Max Combo: " + scoreTracker.getMaxCombo());
        Text countLine = GameUiFactory.createResultText(
                "P " + scoreTracker.getPerfectCount()
                        + "  GR " + scoreTracker.getGreatCount()
                        + "  G " + scoreTracker.getGoodCount()
                        + "  B " + scoreTracker.getBadCount()
                        + "  M " + scoreTracker.getMissCount()
        );
        Text unlockLine = GameUiFactory.createResultText(resultMessage(cleared, unlockedNext));

        Button retryButton = GameUiFactory.createSmallButton("RETRY");
        Button selectButton = GameUiFactory.createSmallButton("STAGES");
        retryButton.setOnAction(e -> controller.startStage(playableStage));
        selectButton.setOnAction(e -> controller.showJourneySelect(playableStage.getJourneyId()));

        HBox buttons = new HBox(16, retryButton, selectButton);
        buttons.setAlignment(Pos.CENTER);

        PlayableStage nextStage = playableStage.next();
        if (nextStage != null && saveManager.isUnlocked(nextStage)) {
            Button nextButton = GameUiFactory.createSmallButton("NEXT");
            nextButton.setOnAction(e -> controller.showLore(nextStage));
            buttons.getChildren().add(nextButton);
        }

        root.getChildren().addAll(
                title,
                gradeText,
                newBestLine,
                scoreLine,
                bonusLine,
                goldLine,
                accuracyLine,
                timingLine,
                timingBalanceLine,
                comboLine,
                countLine,
                unlockLine,
                buttons
        );

        ScaleTransition gradePop = new ScaleTransition(Duration.millis(260), gradeText);
        gradePop.setFromX(0.82);
        gradePop.setFromY(0.82);
        gradePop.setToX(1);
        gradePop.setToY(1);
        gradePop.play();

        return new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
    }

    private String resultMessage(boolean cleared, boolean unlockedNext) {
        if (unlockedNext) {
            return "Next scene unlocked";
        }

        if (cleared) {
            return "Scene cleared";
        }

        return String.format("Reach %.0f%% accuracy to unlock the next scene", GameConfig.CLEAR_ACCURACY);
    }

    private Color gradeColor() {
        return switch (scoreTracker.getGrade()) {
            case "S" -> Color.GOLD;
            case "A" -> Color.LIGHTGREEN;
            case "B" -> Color.DEEPSKYBLUE;
            case "C" -> Color.WHITE;
            case "D" -> Color.ORANGE;
            default -> Color.CRIMSON;
        };
    }
}
