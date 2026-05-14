package model;

import model.Stages.PlayableStage;

public class JourneyScene {
    private final int number;
    private final String title;
    private final String summary;
    private final String storyText;
    private final String lockedReason;
    private final boolean unlockedByDefault;
    private final PlayableStage playableStage;

    public JourneyScene(
            int number,
            String title,
            String summary,
            String storyText,
            String lockedReason,
            boolean unlockedByDefault,
            PlayableStage playableStage
    ) {
        this.number = number;
        this.title = title;
        this.summary = summary;
        this.storyText = storyText;
        this.lockedReason = lockedReason;
        this.unlockedByDefault = unlockedByDefault;
        this.playableStage = playableStage;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getStoryText() {
        return storyText;
    }

    public String getLockedReason() {
        return lockedReason;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    public PlayableStage getPlayableStage() {
        return playableStage;
    }

    public boolean startsGameplay() {
        return playableStage != null;
    }
}
