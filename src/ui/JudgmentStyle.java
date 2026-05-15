package ui;

import score.ScoreTracker;

import java.awt.Color;

public final class JudgmentStyle {
    private JudgmentStyle() {
    }

    public static String text(ScoreTracker.Judgment judgment) {
        return switch (judgment) {
            case PERFECT -> "PERFECT!";
            case GREAT -> "GREAT!";
            case GOOD -> "GOOD!";
            case BAD -> "BAD";
            case MISS -> "MISS";
        };
    }

    public static Color color(ScoreTracker.Judgment judgment) {
        return switch (judgment) {
            case PERFECT -> new Color(255, 215, 0);
            case GREAT -> new Color(0, 191, 255);
            case GOOD -> new Color(144, 238, 144);
            case BAD -> Color.ORANGE;
            case MISS -> new Color(220, 20, 60);
        };
    }
}
