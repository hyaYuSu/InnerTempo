package manager;

import config.GameConfig;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;
import score.ScoreTracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SaveManager {
    private static final Path SAVE_PATH = Path.of("innertempo-save.properties");

    private final boolean[] unlockedWaveStages = new boolean[WaveStage.values().length];
    private final int[] waveHighScores = new int[WaveStage.values().length];
    private final boolean[] unlockedLittleBellStages = new boolean[LittleBellStage.values().length];
    private final int[] littleBellHighScores = new int[LittleBellStage.values().length];

    public SaveManager() {
        unlockedWaveStages[0] = true;
        unlockedLittleBellStages[0] = true;
        load();
    }

    public boolean isUnlocked(PlayableStage stage) {
        return unlockedStagesFor(stage)[stage.getIndex()];
    }

    public int getHighScore(PlayableStage stage) {
        return highScoresFor(stage)[stage.getIndex()];
    }

    public boolean recordResult(PlayableStage stage, ScoreTracker scoreTracker) {
        int stageIndex = stage.getIndex();
        int[] highScores = highScoresFor(stage);
        boolean unlockedNext = false;

        highScores[stageIndex] = Math.max(highScores[stageIndex], scoreTracker.getScore());

        if (scoreTracker.getAccuracy() >= GameConfig.CLEAR_ACCURACY) {
            PlayableStage next = stage.next();

            if (next != null && !isUnlocked(next)) {
                unlockedStagesFor(next)[next.getIndex()] = true;
                unlockedNext = true;
            }
        }

        save();
        return unlockedNext;
    }

    private void load() {
        if (!Files.exists(SAVE_PATH)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(SAVE_PATH)) {
            properties.load(input);

            for (WaveStage stage : WaveStage.values()) {
                loadStage(properties, stage);
            }

            for (LittleBellStage stage : LittleBellStage.values()) {
                loadStage(properties, stage);
            }

            unlockedWaveStages[0] = true;
            unlockedLittleBellStages[0] = true;
        } catch (IOException ignored) {
            unlockedWaveStages[0] = true;
            unlockedLittleBellStages[0] = true;
        }
    }

    private void save() {
        Properties properties = new Properties();

        for (WaveStage stage : WaveStage.values()) {
            saveStage(properties, stage);
        }

        for (LittleBellStage stage : LittleBellStage.values()) {
            saveStage(properties, stage);
        }

        try (OutputStream output = Files.newOutputStream(SAVE_PATH)) {
            properties.store(output, "Inner Tempo progress");
        } catch (IOException ignored) {
        }
    }

    private void loadStage(Properties properties, PlayableStage stage) {
        int index = stage.getIndex();
        unlockedStagesFor(stage)[index] = Boolean.parseBoolean(
                properties.getProperty(key(stage, "unlocked"), index == 0 ? "true" : "false")
        );
        highScoresFor(stage)[index] = parseInt(properties.getProperty(key(stage, "best")), 0);
    }

    private void saveStage(Properties properties, PlayableStage stage) {
        int index = stage.getIndex();
        properties.setProperty(key(stage, "unlocked"), Boolean.toString(unlockedStagesFor(stage)[index]));
        properties.setProperty(key(stage, "best"), Integer.toString(highScoresFor(stage)[index]));
    }

    private boolean[] unlockedStagesFor(PlayableStage stage) {
        if (stage instanceof WaveStage) {
            return unlockedWaveStages;
        }

        if (stage instanceof LittleBellStage) {
            return unlockedLittleBellStages;
        }

        throw new IllegalArgumentException("Unknown playable stage: " + stage);
    }

    private int[] highScoresFor(PlayableStage stage) {
        if (stage instanceof WaveStage) {
            return waveHighScores;
        }

        if (stage instanceof LittleBellStage) {
            return littleBellHighScores;
        }

        throw new IllegalArgumentException("Unknown playable stage: " + stage);
    }

    private String key(PlayableStage stage, String property) {
        return stage.getSaveKeyPrefix() + "." + stage.getNumber() + "." + property;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
