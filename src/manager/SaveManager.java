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
    private final boolean[] clearedWaveStages = new boolean[WaveStage.values().length];
    private final int[] waveHighScores = new int[WaveStage.values().length];
    private final double[] waveBestAccuracies = new double[WaveStage.values().length];
    private final boolean[] unlockedLittleBellStages = new boolean[LittleBellStage.values().length];
    private final boolean[] clearedLittleBellStages = new boolean[LittleBellStage.values().length];
    private final int[] littleBellHighScores = new int[LittleBellStage.values().length];
    private final double[] littleBellBestAccuracies = new double[LittleBellStage.values().length];

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

    public double getBestAccuracy(PlayableStage stage) {
        return bestAccuraciesFor(stage)[stage.getIndex()];
    }

    public boolean isCleared(PlayableStage stage) {
        return clearedStagesFor(stage)[stage.getIndex()];
    }

    public boolean recordResult(PlayableStage stage, ScoreTracker scoreTracker) {
        int stageIndex = stage.getIndex();
        int[] highScores = highScoresFor(stage);
        double[] bestAccuracies = bestAccuraciesFor(stage);
        boolean unlockedNext = false;

        highScores[stageIndex] = Math.max(highScores[stageIndex], scoreTracker.getScore());
        bestAccuracies[stageIndex] = Math.max(bestAccuracies[stageIndex], scoreTracker.getAccuracy());

        if (scoreTracker.getAccuracy() >= GameConfig.CLEAR_ACCURACY) {
            clearedStagesFor(stage)[stageIndex] = true;
            PlayableStage next = stage.next();

            if (next != null && !isUnlocked(next)) {
                unlockedStagesFor(next)[next.getIndex()] = true;
                unlockedNext = true;
            }
        }

        save();
        return unlockedNext;
    }

    public void resetProgress() {
        resetJourney(unlockedWaveStages, clearedWaveStages, waveHighScores, waveBestAccuracies);
        resetJourney(unlockedLittleBellStages, clearedLittleBellStages, littleBellHighScores, littleBellBestAccuracies);
        save();
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
            deriveClearedProgressFromUnlocks(WaveStage.values());
            deriveClearedProgressFromUnlocks(LittleBellStage.values());
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
        clearedStagesFor(stage)[index] = Boolean.parseBoolean(properties.getProperty(key(stage, "cleared"), "false"));
        highScoresFor(stage)[index] = parseInt(properties.getProperty(key(stage, "best")), 0);
        bestAccuraciesFor(stage)[index] = parseDouble(properties.getProperty(key(stage, "bestAccuracy")), 0.0);
    }

    private void saveStage(Properties properties, PlayableStage stage) {
        int index = stage.getIndex();
        properties.setProperty(key(stage, "unlocked"), Boolean.toString(unlockedStagesFor(stage)[index]));
        properties.setProperty(key(stage, "cleared"), Boolean.toString(clearedStagesFor(stage)[index]));
        properties.setProperty(key(stage, "best"), Integer.toString(highScoresFor(stage)[index]));
        properties.setProperty(key(stage, "bestAccuracy"), Double.toString(bestAccuraciesFor(stage)[index]));
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

    private boolean[] clearedStagesFor(PlayableStage stage) {
        if (stage instanceof WaveStage) {
            return clearedWaveStages;
        }

        if (stage instanceof LittleBellStage) {
            return clearedLittleBellStages;
        }

        throw new IllegalArgumentException("Unknown playable stage: " + stage);
    }

    private double[] bestAccuraciesFor(PlayableStage stage) {
        if (stage instanceof WaveStage) {
            return waveBestAccuracies;
        }

        if (stage instanceof LittleBellStage) {
            return littleBellBestAccuracies;
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

    private double parseDouble(String value, double fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void resetJourney(boolean[] unlocked, boolean[] cleared, int[] highScores, double[] bestAccuracies) {
        for (int i = 0; i < unlocked.length; i++) {
            unlocked[i] = i == 0;
            cleared[i] = false;
            highScores[i] = 0;
            bestAccuracies[i] = 0.0;
        }
    }

    private void deriveClearedProgressFromUnlocks(PlayableStage[] stages) {
        for (PlayableStage stage : stages) {
            PlayableStage next = stage.next();
            if (next != null && isUnlocked(next)) {
                clearedStagesFor(stage)[stage.getIndex()] = true;
            }
        }
    }
}
