package screens;

import config.GameConfig;
import content.JourneyCatalog;
import manager.ScreenManager;
import model.Journey;
import model.JourneyScene;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoreScreen {
    private final ScreenManager controller;
    private final PlayableStage playableStage;

    public LoreScreen(ScreenManager controller, PlayableStage playableStage) {
        this.controller = controller;
        this.playableStage = playableStage;
    }

    public Scene create() {
        Journey journey = JourneyCatalog.byId(playableStage.getJourneyId());

        BorderPane root = new BorderPane();
        root.setStyle(backgroundStyle());

        Text bgText = new Text(backgroundLabel(journey));
        bgText.setFill(backgroundTextColor());
        bgText.setFont(Font.font("Georgia", 30));

        Text character = new Text(characterSymbol());
        character.setFill(Color.LIGHTGRAY);
        character.setFont(Font.font(100));

        VBox center = new VBox(40, bgText, character);
        center.setAlignment(Pos.CENTER);

        Text speaker = new Text(speakerName(journey));
        speaker.setFill(Color.GOLDENROD);
        speaker.setFont(Font.font("Georgia", FontWeight.BOLD, 18));

        Text lore = new Text();
        lore.setFill(Color.WHITE);
        lore.setFont(Font.font("Georgia", 22));
        lore.setWrappingWidth(880);

        Text prompt = new Text("SPACE");
        prompt.setFill(Color.LIGHTGRAY);
        prompt.setFont(Font.font("Georgia", FontWeight.BOLD, 14));

        VBox textContent = new VBox(8, speaker, lore, prompt);
        textContent.setAlignment(Pos.CENTER_LEFT);

        StackPane textBox = new StackPane(textContent);
        textBox.setPrefHeight(140);
        textBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.65);"
                        + "-fx-border-color: white;"
                        + "-fx-border-width: 2;"
                        + "-fx-padding: 18 30 18 30;"
        );

        root.setCenter(center);
        root.setBottom(textBox);

        String[] lines = loreLines();
        int[] lineIndex = {0};
        showLoreLine(lore, lines[lineIndex[0]]);

        Scene scene = new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() != KeyCode.SPACE) {
                return;
            }

            lineIndex[0]++;

            if (lineIndex[0] >= lines.length) {
                controller.startStage(playableStage);
                return;
            }

            showLoreLine(lore, lines[lineIndex[0]]);
        });

        return scene;
    }

    private void showLoreLine(Text lore, String line) {
        lore.setText(line);
        lore.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(260), lore);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private String[] loreLines() {
        if (playableStage instanceof WaveStage waveStage) {
            return switch (waveStage) {
                case DRIFT -> new String[] {
                        "The board felt heavier than it looked.",
                        "I stood where the foam reached my feet.",
                        "\"Just one small wave,\" I told myself.",
                        "\"I do not have to be ready for the whole sea.\""
                };
                case FIRST_WAVES -> new String[] {
                        "The first wave lifted me and dropped me fast.",
                        "I came up coughing.",
                        "Then I laughed, because for one second, I had moved with it.",
                        "\"Again,\" I said, before fear could answer."
                };
                case STRUGGLE -> new String[] {
                        "Watching from shore made it look easy.",
                        "Paddling back after every fall did not.",
                        "\"Life does this too,\" I whispered.",
                        "\"It knocks, waits, and comes again.\""
                };
                case STORM -> new String[] {
                        "The sky turned gray.",
                        "The water forgot how to be gentle.",
                        "My hands shook around the board.",
                        "\"Do not beat the wave,\" I told myself. \"Read it.\""
                };
                case BEYOND_THE_ISLAND -> new String[] {
                        "My arms hurt.",
                        "My knees were scraped.",
                        "I was still afraid.",
                        "But when the next wave came, I breathed and said, \"Come on, then.\""
                };
            };
        }

        JourneyScene scene = currentScene();
        if (scene.getStoryText().isBlank()) {
            return new String[] {scene.getSummary()};
        }

        return scene.getStoryText().split("\\n\\n");
    }

    private JourneyScene currentScene() {
        Journey journey = JourneyCatalog.byId(playableStage.getJourneyId());

        for (JourneyScene scene : journey.getScenes()) {
            if (scene.getNumber() == playableStage.getNumber()) {
                return scene;
            }
        }

        throw new IllegalStateException("No scene found for stage " + playableStage.getTitle());
    }

    private String backgroundLabel(Journey journey) {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> "Sunny Room Background";
                case UNDER_THE_TABLE -> "Under Table Background";
                case RAIN_ALLEY -> "Rain Alley Background";
                case WINDOW_LIGHT -> "Window Light Background";
                case LITTLE_BELL -> "Golden-Pink Doorstep Background";
            };
        }

        return journey.getBackgroundLabel();
    }

    private String backgroundStyle() {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> "-fx-background-color: linear-gradient(to bottom, #f5c16c, #6b3f2a);";
                case UNDER_THE_TABLE -> "-fx-background-color: linear-gradient(to bottom, #2b1d18, #090706);";
                case RAIN_ALLEY -> "-fx-background-color: linear-gradient(to bottom, #263b56, #09111c);";
                case WINDOW_LIGHT -> "-fx-background-color: linear-gradient(to bottom, #18223b, #3c241c);";
                case LITTLE_BELL -> "-fx-background-color: linear-gradient(to bottom, #d48a70, #5a3145);";
            };
        }

        return "-fx-background-color: linear-gradient(to bottom, #112f4a, #06111f);";
    }

    private Color backgroundTextColor() {
        if (playableStage instanceof LittleBellStage) {
            return Color.MOCCASIN;
        }

        return Color.LIGHTBLUE;
    }

    private String characterSymbol() {
        if (playableStage instanceof LittleBellStage) {
            return "(=^.^=)";
        }

        return "\u25CF";
    }

    private String speakerName(Journey journey) {
        if (playableStage instanceof LittleBellStage) {
            return "Wandering Cat";
        }

        return journey.getTitle().equals("Waves") ? "Me" : journey.getTitle();
    }
}
