package model.Stages;

import model.JourneyId;

public enum WaveStage implements PlayableStage {
    DRIFT(1, "Drift"),
    FIRST_WAVES(2, "First Waves"),
    STRUGGLE(3, "Struggle"),
    STORM(4, "Storm"),
    BEYOND_THE_ISLAND(5, "Beyond the Island");

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
