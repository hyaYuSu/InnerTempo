package ui;

import model.JourneyId;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;
import javafx.scene.paint.Color;

public class JourneyTheme {
    private final String gameplayBackgroundStyle;
    private final Color accentColor;
    private final Color softTextColor;
    private final Color pulseColor;
    private final Color goldNoteColor;
    private final Color[] laneColors;

    private JourneyTheme(
            String gameplayBackgroundStyle,
            Color accentColor,
            Color softTextColor,
            Color pulseColor,
            Color goldNoteColor,
            Color[] laneColors
    ) {
        this.gameplayBackgroundStyle = gameplayBackgroundStyle;
        this.accentColor = accentColor;
        this.softTextColor = softTextColor;
        this.pulseColor = pulseColor;
        this.goldNoteColor = goldNoteColor;
        this.laneColors = laneColors;
    }

    public static JourneyTheme forStage(PlayableStage stage) {
        if (stage.getJourneyId() == JourneyId.GATO) {
            return littleBell();
        }

        if (stage == WaveStage.FIRST_WAVES) {
            return firstWaves();
        }

        return waves();
    }

    public String getGameplayBackgroundStyle() {
        return gameplayBackgroundStyle;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public Color getSoftTextColor() {
        return softTextColor;
    }

    public Color getPulseColor() {
        return pulseColor;
    }

    public Color getGoldNoteColor() {
        return goldNoteColor;
    }

    public Color laneColor(int lane) {
        if (lane < 0 || lane >= laneColors.length) {
            return Color.WHITE;
        }

        return laneColors[lane];
    }

    public static JourneyTheme waves() {
        return new JourneyTheme(
                "-fx-background-color: linear-gradient(to bottom, #06111f, #020407);",
                Color.GOLDENROD,
                Color.LIGHTBLUE,
                Color.rgb(103, 202, 255),
                Color.GOLD,
                new Color[] {
                        Color.rgb(255, 92, 122),
                        Color.rgb(89, 198, 255),
                        Color.rgb(120, 230, 150),
                        Color.rgb(255, 202, 84)
                }
        );
    }

    public static JourneyTheme firstWaves() {
        return new JourneyTheme(
                "-fx-background-color: linear-gradient(to bottom, #082436, #03131a 58%, #010406);",
                Color.rgb(91, 216, 223),
                Color.rgb(177, 234, 238),
                Color.rgb(90, 230, 216),
                Color.rgb(255, 219, 92),
                new Color[] {
                        Color.rgb(105, 207, 255),
                        Color.rgb(94, 236, 190),
                        Color.rgb(250, 213, 102),
                        Color.rgb(255, 125, 166)
                }
        );
    }

    public static JourneyTheme littleBell() {
        return new JourneyTheme(
                "-fx-background-color: linear-gradient(to bottom, #1c1411, #070504);",
                Color.rgb(239, 177, 92),
                Color.rgb(255, 226, 184),
                Color.rgb(255, 188, 108),
                Color.rgb(255, 217, 92),
                new Color[] {
                        Color.rgb(247, 123, 104),
                        Color.rgb(246, 183, 92),
                        Color.rgb(168, 214, 145),
                        Color.rgb(180, 153, 236)
                }
        );
    }
}
