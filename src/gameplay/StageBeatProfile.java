package gameplay;

import model.Stages.PlayableStage;
import model.Stages.LittleBellStage;
import model.Stages.WaveStage;

import java.util.Optional;

public record StageBeatProfile(
        double firstBeatOffset,
        double beatSeconds,
        int beatsPerMeasure,
        int gridSlotsPerBeat
) {
    public static Optional<StageBeatProfile> forStage(PlayableStage stage) {
        if (stage == WaveStage.DRIFT) {
            return Optional.of(drift());
        }

        if (stage == WaveStage.FIRST_WAVES) {
            return Optional.of(firstWaves());
        }

        if (stage == WaveStage.STRUGGLE) {
            return Optional.of(struggle());
        }

        if (stage == WaveStage.STORM) {
            return Optional.of(steadyRhythm());
        }

        if (stage == WaveStage.BEYOND_THE_ISLAND || stage == WaveStage.ONE_PULL_ONE_RELEASE) {
            return Optional.of(pathOfLight());
        }

        if (stage instanceof LittleBellStage littleBellStage) {
            return Optional.of(forLittleBellStage(littleBellStage));
        }

        return Optional.empty();
    }

    public static StageBeatProfile drift() {
        return new StageBeatProfile(0.0001, 60.0 / 80.0, 4, 4);
    }

    public static StageBeatProfile firstWaves() {
        return new StageBeatProfile(0.52245, 60.0 / 100.0, 4, 8);
    }

    public static StageBeatProfile struggle() {
        return new StageBeatProfile(0.0002, 60.0 / 80.0, 4, 4);
    }

    public static StageBeatProfile steadyRhythm() {
        return new StageBeatProfile(2.1818, 60.0 / 110.0, 4, 4);
    }

    public static StageBeatProfile pathOfLight() {
        return new StageBeatProfile(0.0012, 60.0 / 90.0, 4, 4);
    }

    private static StageBeatProfile forLittleBellStage(LittleBellStage stage) {
        return switch (stage) {
            case EMPTY_BASKET -> drift();
            case UNDER_THE_TABLE -> firstWaves();
            case RAIN_ALLEY -> struggle();
            case WINDOW_LIGHT -> steadyRhythm();
            case LITTLE_BELL -> pathOfLight();
        };
    }

    public double sixteenthSeconds() {
        return beatSeconds / 4.0;
    }

    public double gridSlotSeconds() {
        return beatSeconds / gridSlotsPerBeat;
    }

    public double timeForGridSlot(int measure, int slot) {
        return firstBeatOffset + ((measure * beatsPerMeasure * gridSlotsPerBeat + slot) * gridSlotSeconds());
    }
}
