package model;

import java.util.List;

public class Journey {
    private final JourneyId id;
    private final String title;
    private final String subtitle;
    private final String description;
    private final String backgroundLabel;
    private final List<JourneyScene> scenes;

    public Journey(
            JourneyId id,
            String title,
            String subtitle,
            String description,
            String backgroundLabel,
            List<JourneyScene> scenes
    ) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.backgroundLabel = backgroundLabel;
        this.scenes = scenes;
    }

    public JourneyId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getBackgroundLabel() {
        return backgroundLabel;
    }

    public List<JourneyScene> getScenes() {
        return scenes;
    }
}
