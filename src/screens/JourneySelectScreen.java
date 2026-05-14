package screens;

import config.GameConfig;
import content.JourneyCatalog;
import manager.SaveManager;
import manager.ScreenManager;
import model.Journey;
import model.JourneyId;
import model.JourneyScene;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;
import ui.GameUiFactory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class JourneySelectScreen {
    private final ScreenManager controller;
    private final SaveManager saveManager;
    private final Journey selectedJourney;

    public JourneySelectScreen(ScreenManager controller, SaveManager saveManager, JourneyId selectedJourneyId) {
        this.controller = controller;
        this.saveManager = saveManager;
        this.selectedJourney = JourneyCatalog.byId(selectedJourneyId);
    }

    public Scene create() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #050505;");

        root.setLeft(createJourneyMenu());
        root.setCenter(createJourneyDetails());

        return new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
    }

    private VBox createJourneyMenu() {
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(50));
        leftPanel.setPrefWidth(300);

        Text playTitle = new Text("PLAY");
        playTitle.setFill(Color.WHITE);
        playTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 46));

        Text pickStage = new Text("Pick a Journey");
        pickStage.setFill(Color.LIGHTGRAY);
        pickStage.setFont(Font.font("Georgia", 24));

        Button waves = GameUiFactory.createMenuButton("Waves");
        Button stage2 = GameUiFactory.createMenuButton("Little Bell");
        Button comingSoon = GameUiFactory.createMenuButton("Coming Soon");
        waves.setOnAction(e -> controller.showJourneySelect(JourneyId.WAVES));
        stage2.setOnAction(e -> controller.showJourneySelect(JourneyId.GATO));
        comingSoon.setDisable(true);

        Button backButton = GameUiFactory.createSmallButton("BACK");
        backButton.setOnAction(e -> controller.showMainMenu());

        leftPanel.getChildren().addAll(playTitle, pickStage, waves, stage2, comingSoon, backButton);
        return leftPanel;
    }

    private VBox createJourneyDetails() {
        VBox rightPanel = new VBox(13);
        rightPanel.setPadding(new Insets(55, 60, 40, 60));
        rightPanel.setAlignment(Pos.TOP_LEFT);

        Text title = new Text(selectedJourney.getTitle());
        title.setFill(Color.GOLDENROD);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 34));

        Text subtitle = new Text(selectedJourney.getSubtitle());
        subtitle.setFill(Color.LIGHTGRAY);
        subtitle.setFont(Font.font("Georgia", 20));

        Text description = new Text(selectedJourney.getDescription());
        description.setFill(Color.WHITE);
        description.setFont(Font.font("Georgia", 18));
        description.setWrappingWidth(580);

        Text progress = new Text(progressText());
        progress.setFill(Color.LIGHTBLUE);
        progress.setFont(Font.font("Georgia", FontWeight.BOLD, 18));

        Text previewTitle = new Text("Hover a scene");
        previewTitle.setFill(Color.WHITE);
        previewTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 22));

        Text previewMeta = new Text("Difficulty, BPM, best score, and lock state appear here.");
        previewMeta.setFill(Color.LIGHTBLUE);
        previewMeta.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        previewMeta.setWrappingWidth(560);

        Text previewDetails = new Text("Scene details and unlock requirements appear here.");
        previewDetails.setFill(Color.LIGHTGRAY);
        previewDetails.setFont(Font.font("Georgia", 17));
        previewDetails.setWrappingWidth(580);

        VBox previewPanel = new VBox(8, previewTitle, previewMeta, previewDetails);
        previewPanel.setPadding(new Insets(16));
        previewPanel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.045);"
                        + "-fx-border-color: rgba(255,255,255,0.22);"
                        + "-fx-border-width: 1;"
        );

        rightPanel.getChildren().addAll(title, subtitle, description, progress, previewPanel);

        for (JourneyScene scene : selectedJourney.getScenes()) {
            boolean unlocked = isUnlocked(scene);
            Button stageButton = GameUiFactory.createStageBoard(stageButtonText(scene, unlocked), unlocked);
            stageButton.addEventHandler(
                    MouseEvent.MOUSE_ENTERED,
                    e -> updatePreview(scene, unlocked, previewTitle, previewMeta, previewDetails)
            );

            if (unlocked) {
                stageButton.setOnAction(e -> openScene(scene));
            }

            rightPanel.getChildren().add(stageButton);
        }

        return rightPanel;
    }

    private void openScene(JourneyScene scene) {
        if (scene.startsGameplay()) {
            controller.showLore(scene.getPlayableStage());
        } else {
            controller.showJourneyScene(selectedJourney, scene);
        }
    }

    private boolean isUnlocked(JourneyScene scene) {
        PlayableStage playableStage = scene.getPlayableStage();

        if (playableStage != null) {
            return saveManager.isUnlocked(playableStage);
        }

        return scene.isUnlockedByDefault();
    }

    private String stageButtonText(JourneyScene scene, boolean unlocked) {
        if (unlocked) {
            String text = "Scene " + scene.getNumber() + ": " + scene.getTitle();

            if (scene.getPlayableStage() != null) {
                text += "   Best: " + saveManager.getHighScore(scene.getPlayableStage());
            }

            return text;
        }

        if (scene.getNumber() == selectedJourney.getScenes().size()) {
            return "Locked - Final Scene";
        }

        return "Locked - Scene " + scene.getNumber();
    }

    private void updatePreview(
            JourneyScene scene,
            boolean unlocked,
            Text previewTitle,
            Text previewMeta,
            Text previewDetails
    ) {
        previewTitle.setText("Scene " + scene.getNumber() + ": " + scene.getTitle());
        previewMeta.setText(previewMetaText(scene, unlocked));

        if (unlocked) {
            previewDetails.setText(scene.getSummary() + "\nStatus: Unlocked");
            return;
        }

        previewDetails.setText(scene.getSummary() + "\nLocked: " + scene.getLockedReason());
    }

    private String previewMetaText(JourneyScene scene, boolean unlocked) {
        PlayableStage playableStage = scene.getPlayableStage();

        if (playableStage == null) {
            return unlocked ? "Story scene | Unlocked" : "Story scene | Locked";
        }

        return difficultyText(playableStage)
                + " | "
                + bpmText(playableStage)
                + " | Best "
                + saveManager.getHighScore(playableStage)
                + " | "
                + (unlocked ? "Unlocked" : "Locked");
    }

    private String difficultyText(PlayableStage stage) {
        int level = Math.min(5, stage.getIndex() + 1);

        return "Difficulty " + level + "/5";
    }

    private String bpmText(PlayableStage stage) {
        if (stage == WaveStage.DRIFT) {
            return "80 BPM";
        }

        if (stage == WaveStage.FIRST_WAVES) {
            return "100 BPM";
        }

        if (stage instanceof WaveStage) {
            return "Generated rhythm";
        }

        if (stage instanceof LittleBellStage) {
            return "Story rhythm";
        }

        return "Rhythm stage";
    }

    private String progressText() {
        int unlocked = 0;

        for (JourneyScene scene : selectedJourney.getScenes()) {
            if (isUnlocked(scene)) {
                unlocked++;
            }
        }

        return "Progress: " + unlocked + " / " + selectedJourney.getScenes().size() + " scenes unlocked";
    }
}
