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
            return waveStory(waveStage, cleared) + rhythmReflection(scoreTracker);
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

    private static String waveStory(WaveStage stage, boolean cleared) {
        if (!cleared) {
            return switch (stage) {
                case DRIFT -> "I reached for the first wave before I understood it. The water passed under me, but the board stayed close.";
                case FIRST_WAVES -> "I fell before the ride began. Salt filled my mouth, and the next wave was already forming.";
                case STRUGGLE -> "I watched the wave go on without me. It did not feel cruel. It only reminded me that I had to paddle back.";
                case STORM -> "The storm took my balance before I could find the rhythm. I came up breathing hard, still holding the board.";
                case BEYOND_THE_ISLAND -> "I tried to move with the water, but fear pulled me stiff again. The sea kept going, patient and wide.";
            };
        }

        return switch (stage) {
            case DRIFT -> "I did not ride far, but I stayed on long enough to feel the board answer. A small wave was still a beginning.";
            case FIRST_WAVES -> "I fell, came up laughing, and paddled back. The sea had not become easier. I had become willing to try again.";
            case STRUGGLE -> "The wave threw me under, but not away from myself. I learned that getting back on the board was part of the ride.";
            case STORM -> "The storm stayed loud, but I stopped trying to silence it. I leaned, breathed, and let the wave carry me through.";
            case BEYOND_THE_ISLAND -> "I was tired, afraid, and moving anyway. Life kept coming in waves, and I finally knew how to meet one.";
        };
    }

    private static String littleBellStory(LittleBellStage stage, boolean cleared) {
        if (!cleared) {
            return "The sound fades before the cat can follow it. Somewhere nearby, the small bell waits.";
        }

        return switch (stage) {
            case EMPTY_BASKET -> "The cat steps out of the basket. The missing toy has left a silence shaped like a question.";
            case UNDER_THE_TABLE -> "The button was not the bell, but it taught the cat where the sound was not.";
            case RAIN_ALLEY -> "Rain makes every sound smaller. Still, one chime carries through the alley.";
            case WINDOW_LIGHT -> "The warm window remembers something, even if it cannot open.";
            case LITTLE_BELL -> "The bell is small, but it rings. Some things return as sound, not shape.";
        };
    }
}
