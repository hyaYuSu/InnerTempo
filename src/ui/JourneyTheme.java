package ui;

import model.JourneyId;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import java.awt.Color;

public class JourneyTheme {
    private final Color backgroundTop;
    private final Color backgroundBottom;
    private final Color accentColor;
    private final Color softTextColor;
    private final Color pulseColor;
    private final Color goldNoteColor;
    private final Color[] laneColors;

    private JourneyTheme(
            Color backgroundTop,
            Color backgroundBottom,
            Color accentColor,
            Color softTextColor,
            Color pulseColor,
            Color goldNoteColor,
            Color[] laneColors
    ) {
        this.backgroundTop = backgroundTop;
        this.backgroundBottom = backgroundBottom;
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

    public Color getBackgroundTop() {
        return backgroundTop;
    }

    public Color getBackgroundBottom() {
        return backgroundBottom;
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
                new Color(6, 17, 31),
                new Color(2, 4, 7),
                new Color(218, 165, 32),
                new Color(173, 216, 230),
                new Color(103, 202, 255),
                new Color(255, 215, 0),
                new Color[] {
                        new Color(255, 92, 122),
                        new Color(89, 198, 255),
                        new Color(120, 230, 150),
                        new Color(255, 202, 84)
                }
        );
    }

    public static JourneyTheme firstWaves() {
        return new JourneyTheme(
                new Color(8, 36, 54),
                new Color(1, 4, 6),
                new Color(91, 216, 223),
                new Color(177, 234, 238),
                new Color(90, 230, 216),
                new Color(255, 219, 92),
                new Color[] {
                        new Color(105, 207, 255),
                        new Color(94, 236, 190),
                        new Color(250, 213, 102),
                        new Color(255, 125, 166)
                }
        );
    }

    public static JourneyTheme littleBell() {
        return new JourneyTheme(
                new Color(28, 20, 17),
                new Color(7, 5, 4),
                new Color(239, 177, 92),
                new Color(255, 226, 184),
                new Color(255, 188, 108),
                new Color(255, 217, 92),
                new Color[] {
                        new Color(247, 123, 104),
                        new Color(246, 183, 92),
                        new Color(168, 214, 145),
                        new Color(180, 153, 236)
                }
        );
    }
}
