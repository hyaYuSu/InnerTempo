package settings;

import model.Stages.PlayableStage;

import java.util.HashMap;
import java.util.Map;

public class GameplaySettings {
    private static final double MIN_STAGE_CHART_OFFSET = -0.30;
    private static final double MAX_STAGE_CHART_OFFSET = 0.30;

    private final Map<String, Double> stageChartOffsets = new HashMap<>();

    private double noteSpeedMultiplier = 1.0;
    private double timingWindowScale = 1.0;
    private double inputOffsetSeconds = 0.0;

    public double getNoteSpeedMultiplier() {
        return noteSpeedMultiplier;
    }

    public double getTimingWindowScale() {
        return timingWindowScale;
    }

    public double getInputOffsetSeconds() {
        return inputOffsetSeconds;
    }

    public double getStageChartOffsetSeconds(PlayableStage stage) {
        return stageChartOffsets.getOrDefault(stageKey(stage), 0.0);
    }

    public void decreaseNoteSpeed() {
        noteSpeedMultiplier = Math.max(0.7, noteSpeedMultiplier - 0.1);
    }

    public void increaseNoteSpeed() {
        noteSpeedMultiplier = Math.min(1.5, noteSpeedMultiplier + 0.1);
    }

    public void useStrictTiming() {
        timingWindowScale = 0.85;
    }

    public void useNormalTiming() {
        timingWindowScale = 1.0;
    }

    public void useRelaxedTiming() {
        timingWindowScale = 1.2;
    }

    public void adjustInputOffsetMillis(int millis) {
        inputOffsetSeconds = Math.max(-0.15, Math.min(0.15, inputOffsetSeconds + millis / 1000.0));
    }

    public void resetInputOffset() {
        inputOffsetSeconds = 0;
    }

    public void adjustStageChartOffsetMillis(PlayableStage stage, int millis) {
        double nextOffset = getStageChartOffsetSeconds(stage) + millis / 1000.0;
        stageChartOffsets.put(stageKey(stage), clamp(nextOffset, MIN_STAGE_CHART_OFFSET, MAX_STAGE_CHART_OFFSET));
    }

    public void resetStageChartOffset(PlayableStage stage) {
        stageChartOffsets.remove(stageKey(stage));
    }

    public String timingLabel() {
        if (timingWindowScale < 1.0) return "Strict";
        if (timingWindowScale > 1.0) return "Relaxed";
        return "Normal";
    }

    private String stageKey(PlayableStage stage) {
        return stage.getSaveKeyPrefix() + "." + stage.getNumber();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
