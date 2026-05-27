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
            return switch (stage) {
                case EMPTY_BASKET -> "Little Bell loses the rhythm in the fog. The basket stays between his teeth, still empty and still heavy.";
                case UNDER_THE_TABLE -> "The shadows behind the crates grow too loud. Little Bell flinches back before he can pass through them.";
                case RAIN_ALLEY -> "The rusted stairs hold their cold. Little Bell remembers the red toy, but the memory slips away before it can warm him.";
                case WINDOW_LIGHT -> "The basket feels too heavy to lift. Little Bell sinks closer to the alley, still wishing for warmth.";
                case THRESHOLD_LIGHT -> "The yellow light waits beyond the fog, but Little Bell cannot step into it yet. The voice inside him still says to hide.";
                case LITTLE_BELL -> "The door opens, but Little Bell cannot believe the welcome yet. He looks back at the empty basket and trembles.";
            };
        }

        return switch (stage) {
            case EMPTY_BASKET -> "Little Bell keeps walking through the fog with the empty basket. It is heavy, but he has not let go.";
            case UNDER_THE_TABLE -> "The city watches from the crates and shadows. Little Bell is afraid, but he keeps searching for the joy he lost.";
            case RAIN_ALLEY -> "Under the rusted stairs, Little Bell remembers the bright red toy. The memory hurts, but it proves warmth was real.";
            case WINDOW_LIGHT -> "Little Bell is shaking and tired, but he refuses to become another piece of the alley.";
            case THRESHOLD_LIGHT -> "The warm yellow light cuts through the fog. Little Bell steps toward it, even with an empty basket.";
            case LITTLE_BELL -> "The kind voice sees Little Bell, not the void in the basket. He does not need the toy to be worthy of the light.";
        };
    }
}
