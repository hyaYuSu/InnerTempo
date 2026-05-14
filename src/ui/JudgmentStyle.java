package ui;

import score.ScoreTracker;

import javafx.scene.paint.Color;

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
            case PERFECT -> Color.GOLD;
            case GREAT -> Color.DEEPSKYBLUE;
            case GOOD -> Color.LIGHTGREEN;
            case BAD -> Color.ORANGE;
            case MISS -> Color.CRIMSON;
        };
    }
}
