package model.Stages;

import model.JourneyId;

public interface PlayableStage {
    JourneyId getJourneyId();

    int getNumber();

    int getIndex();

    String getTitle();

    PlayableStage next();

    String getSaveKeyPrefix();
}
