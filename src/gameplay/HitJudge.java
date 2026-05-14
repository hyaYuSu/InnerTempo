package gameplay;

import score.ScoreTracker;
import settings.GameplaySettings;

public class HitJudge {
    private static final double PERFECT_WINDOW = 0.07;
    private static final double GREAT_WINDOW = 0.09;
    private static final double GOOD_WINDOW = 0.135;
    private static final double BAD_WINDOW = 0.18;

    private final GameplaySettings options;

    public HitJudge(GameplaySettings options) {
        this.options = options;
    }

    public ScoreTracker.Judgment judge(double offsetSeconds) {
        double offset = Math.abs(offsetSeconds);

        if (offset <= scaled(PERFECT_WINDOW)) return ScoreTracker.Judgment.PERFECT;
        if (offset <= scaled(GREAT_WINDOW)) return ScoreTracker.Judgment.GREAT;
        if (offset <= scaled(GOOD_WINDOW)) return ScoreTracker.Judgment.GOOD;
        return ScoreTracker.Judgment.BAD;
    }

    public boolean canHit(double offsetSeconds) {
        return Math.abs(offsetSeconds) <= badWindow();
    }

    public double badWindow() {
        return scaled(BAD_WINDOW);
    }

    public ScoreTracker.Judgment worse(ScoreTracker.Judgment first, ScoreTracker.Judgment second) {
        return rank(first) <= rank(second) ? first : second;
    }

    private double scaled(double timingWindow) {
        return timingWindow * options.getTimingWindowScale();
    }

    private int rank(ScoreTracker.Judgment judgment) {
        return switch (judgment) {
            case PERFECT -> 4;
            case GREAT -> 3;
            case GOOD -> 2;
            case BAD -> 1;
            case MISS -> 0;
        };
    }
}
