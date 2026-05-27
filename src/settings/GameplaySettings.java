package settings;

import java.awt.event.KeyEvent;

public class GameplaySettings {
    private static final int MIN_VOLUME_PERCENT = 0;
    private static final int MAX_VOLUME_PERCENT = 100;
    private static final int VOLUME_STEP_PERCENT = 10;

    private double noteSpeedMultiplier = 1.0;
    private double timingWindowScale = 1.0;
    private int musicVolumePercent = 80;
    private final int[] laneKeyCodes = {KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT};

    public double getNoteSpeedMultiplier() {
        return noteSpeedMultiplier;
    }

    public double getTimingWindowScale() {
        return timingWindowScale;
    }

    public double getMusicVolume() {
        return musicVolumePercent / 100.0;
    }

    public int getMusicVolumePercent() {
        return musicVolumePercent;
    }

    public void setMusicVolumePercent(int musicVolumePercent) {
        this.musicVolumePercent = Math.max(MIN_VOLUME_PERCENT, Math.min(MAX_VOLUME_PERCENT, musicVolumePercent));
    }

    public String volumeLabel() {
        return "Volume: " + musicVolumePercent + "%";
    }

    public String controlsLabel() {
        return "Controls: " + keyLabelForLane(0)
                + "  " + keyLabelForLane(1)
                + "  " + keyLabelForLane(2)
                + "  " + keyLabelForLane(3);
    }

    public int[] laneKeyCodes() {
        return laneKeyCodes.clone();
    }

    public int laneForKey(int keyCode) {
        for (int lane = 0; lane < laneKeyCodes.length; lane++) {
            if (laneKeyCodes[lane] == keyCode) {
                return lane;
            }
        }

        return -1;
    }

    public void decreaseVolume() {
        musicVolumePercent = Math.max(MIN_VOLUME_PERCENT, musicVolumePercent - VOLUME_STEP_PERCENT);
    }

    public void increaseVolume() {
        musicVolumePercent = Math.min(MAX_VOLUME_PERCENT, musicVolumePercent + VOLUME_STEP_PERCENT);
    }

    public String keyLabelForLane(int lane) {
        if (lane < 0 || lane >= laneKeyCodes.length) {
            return "?";
        }

        return KeyEvent.getKeyText(laneKeyCodes[lane]);
    }

    public void setLaneKeyCode(int lane, int keyCode) {
        if (lane < 0 || lane >= laneKeyCodes.length || isReservedGameplayKey(keyCode)) {
            return;
        }

        int previousKey = laneKeyCodes[lane];
        for (int otherLane = 0; otherLane < laneKeyCodes.length; otherLane++) {
            if (otherLane != lane && laneKeyCodes[otherLane] == keyCode) {
                laneKeyCodes[otherLane] = previousKey;
            }
        }

        laneKeyCodes[lane] = keyCode;
    }

    private boolean isReservedGameplayKey(int keyCode) {
        return keyCode == KeyEvent.VK_ESCAPE;
    }

    public void resetControls() {
        laneKeyCodes[0] = KeyEvent.VK_LEFT;
        laneKeyCodes[1] = KeyEvent.VK_UP;
        laneKeyCodes[2] = KeyEvent.VK_DOWN;
        laneKeyCodes[3] = KeyEvent.VK_RIGHT;
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

    public String timingLabel() {
        if (timingWindowScale < 1.0) return "Strict";
        if (timingWindowScale > 1.0) return "Relaxed";
        return "Normal";
    }
}
