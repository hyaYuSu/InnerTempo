package model.Stages;

import model.JourneyId;

public enum LittleBellStage implements PlayableStage {
    EMPTY_BASKET(1, "Empty Basket"),
    UNDER_THE_TABLE(2, "Under the Table"),
    RAIN_ALLEY(3, "Rain Alley"),
    WINDOW_LIGHT(4, "Window Light"),
    LITTLE_BELL(5, "Little Bell");

    private final int number;
    private final String title;

    LittleBellStage(int number, String title) {
        this.number = number;
        this.title = title;
    }

    @Override
    public JourneyId getJourneyId() {
        return JourneyId.GATO;
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
    public LittleBellStage next() {
        int nextNumber = number + 1;

        for (LittleBellStage stage : values()) {
            if (stage.number == nextNumber) {
                return stage;
            }
        }

        return null;
    }

    @Override
    public String getSaveKeyPrefix() {
        return "littleBell.stage";
    }
}
