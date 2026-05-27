package content;

import config.GameConfig;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;
import score.ScoreTracker;

public final class StageResultStoryCatalog {
    private StageResultStoryCatalog() {
    }

    public static String titleFor(PlayableStage stage, ScoreTracker scoreTracker) {
        if (scoreTracker.getAccuracy() >= GameConfig.CLEAR_ACCURACY) {
            return stage.getTitle() + " Cleared";
        }

        return stage.getTitle() + " Echo";
    }

    public static String storyFor(PlayableStage stage, ScoreTracker scoreTracker) {
        boolean cleared = scoreTracker.getAccuracy() >= GameConfig.CLEAR_ACCURACY;

        if (stage instanceof WaveStage waveStage) {
            String story = WavesStoryCatalog.resultFor(waveStage, cleared);
            if (waveStage == WaveStage.ONE_PULL_ONE_RELEASE && cleared) {
                return story;
            }

            return story + rhythmReflection(scoreTracker);
        }

        if (stage instanceof LittleBellStage littleBellStage) {
            return littleBellStory(littleBellStage, cleared) + rhythmReflection(scoreTracker);
        }

        return cleared
                ? "The rhythm settles for a moment."
                : "The rhythm slips away, waiting to be heard again.";
    }

    private static String rhythmReflection(ScoreTracker scoreTracker) {
        if (scoreTracker.getTimedHitCount() == 0) {
            return "";
        }

        double averageBias = scoreTracker.getAverageSignedTimingOffsetMillis();

        if (Math.abs(averageBias) >= 45) {
            return averageBias > 0
                    ? "\n\nMost of the rhythm landed late, like the body was answering after the moment had already passed."
                    : "\n\nMost of the rhythm landed early, like the body was reaching for the beat before it arrived.";
        }

        if (scoreTracker.getAverageAbsoluteTimingOffsetMillis() <= 35 && scoreTracker.getAccuracy() >= 85) {
            return "\n\nThe timing settled close to the pulse. For a moment, the scene and the rhythm moved together.";
        }

        if (scoreTracker.getGoldMissCount() > scoreTracker.getGoldHitCount()) {
            return "\n\nSome important beats slipped past. The story still moved, but its brightest moments were harder to catch.";
        }

        return "\n\nThe rhythm was uneven, but it stayed readable enough to carry the scene forward.";
    }

    private static String littleBellStory(LittleBellStage stage, boolean cleared) {
        if (!cleared) {
            return "The fog folds back around Little Bell. The basket is still empty, and the path waits to be walked again.";
        }

        return switch (stage) {
            case EMPTY_BASKET -> "Little Bell keeps hold of the basket, even when nothing inside it feels heavier than stone.";
            case UNDER_THE_TABLE -> "The city watches, but Little Bell keeps moving through the crates and shadows.";
            case RAIN_ALLEY -> "The cold corner cannot make Little Bell disappear, even when the old joy feels lost.";
            case WINDOW_LIGHT -> "Little Bell is tired, but still refuses to become another piece of the alley.";
            case THRESHOLD_LIGHT -> "The warm light cuts through the fog, and Little Bell chooses to step toward it.";
            case LITTLE_BELL -> "Little Bell is seen without the toy, without the old self, and that is enough.";
        };
    }
}
