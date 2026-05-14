package gameplay;

import model.Stages.PlayableStage;
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

        return Optional.empty();
    }

    public static StageBeatProfile drift() {
        return new StageBeatProfile(0.021, 60.0 / 80.0, 4, 4);
    }

    public static StageBeatProfile firstWaves() {
        return new StageBeatProfile(0.000, 60.0 / 100.0, 4, 8);
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
