package model.Stages;

import model.JourneyId;

public enum WaveStage implements PlayableStage {
    DRIFT(1, "Adrift"),
    FIRST_WAVES(2, "Small Waves"),
    STRUGGLE(3, "Rough Water"),
    STORM(4, "Steady Rhythm"),
    BEYOND_THE_ISLAND(5, "Path of Light"),
    ONE_PULL_ONE_RELEASE(6, "One Pull, One Release");

    private final int number;
    private final String title;

    WaveStage(int number, String title) {
        this.number = number;
        this.title = title;
    }

    @Override
    public JourneyId getJourneyId() {
        return JourneyId.WAVES;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getIndex() {
        return number - 1;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public WaveStage next() {
        int nextNumber = number + 1;

        for (WaveStage stage : values()) {
            if (stage.number == nextNumber) {
                return stage;
            }
        }

        return null;
    }

    @Override
    public String getSaveKeyPrefix() {
        return "waves.stage";
    }
}
