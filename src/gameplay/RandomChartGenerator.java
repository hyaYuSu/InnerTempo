package gameplay;

import model.Stages.LittleBellStage;
import model.Note;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomChartGenerator {
    private static final Random RANDOM = new Random();

    private final ManualChartGenerator manualChartGenerator;

    public RandomChartGenerator() {
        this.manualChartGenerator = new ManualChartGenerator();
    }

    public List<Note> generateChart(PlayableStage stage) {
        return manualChartGenerator.generate(stage)
                .orElseGet(() -> generate(ChartSettings.forStage(stage)));
    }

    private List<Note> generate(ChartSettings settings) {
        List<Note> notes = new ArrayList<>();

        double time = settings.firstNoteTime();
        int previousLane = -1;
        int phrase = 0;

        while (time < settings.durationSeconds()) {
            if (phrase % settings.restEveryPhrases() == settings.restEveryPhrases() - 1) {
                time += settings.restLength();
                phrase++;
                continue;
            }

            if (phrase % settings.holdEveryPhrases() == settings.holdEveryPhrases() - 2) {
                previousLane = addHoldPattern(notes, time, settings, previousLane);
                time += settings.holdDuration() + settings.step();
                phrase++;
                continue;
            }

            int pattern = RANDOM.nextInt(settings.patternTypes());

            if (pattern == 0) {
                previousLane = addAlternatingPattern(notes, time, settings, previousLane);
                time += settings.step() * 4;
            } else if (pattern == 1) {
                previousLane = addWalkPattern(notes, time, settings, previousLane);
                time += settings.step() * 4;
            } else if (pattern == 2) {
                previousLane = addBurstPattern(notes, time, settings, previousLane);
                time += settings.step() * 3;
            } else {
                previousLane = addHoldPattern(notes, time, settings, previousLane);
                time += settings.holdDuration() + settings.step();
            }

            phrase++;
        }

        return notes;
    }

    private int addAlternatingPattern(List<Note> notes, double start, ChartSettings settings, int previousLane) {
        int firstLane = nextLane(previousLane);
        int secondLane = mirroredLane(firstLane);

        notes.add(new Note(start, firstLane));
        notes.add(new Note(start + settings.step(), secondLane));
        notes.add(new Note(start + settings.step() * 2, firstLane));
        notes.add(new Note(start + settings.step() * 3, secondLane));

        return secondLane;
    }

    private int addWalkPattern(List<Note> notes, double start, ChartSettings settings, int previousLane) {
        int lane = nextLane(previousLane);
        int direction = RANDOM.nextBoolean() ? 1 : -1;

        for (int i = 0; i < 4; i++) {
            notes.add(new Note(start + settings.step() * i, lane));
            lane = Math.floorMod(lane + direction, 4);
        }

        return Math.floorMod(lane - direction, 4);
    }

    private int addBurstPattern(List<Note> notes, double start, ChartSettings settings, int previousLane) {
        int lane = nextLane(previousLane);
        double burstStep = settings.step() * 0.55;

        for (int i = 0; i < 3; i++) {
            notes.add(new Note(start + burstStep * i, lane));
            lane = nextLane(lane);
        }

        return lane;
    }

    private int addHoldPattern(List<Note> notes, double start, ChartSettings settings, int previousLane) {
        int lane = nextLane(previousLane);
        notes.add(new Note(start, lane, settings.holdDuration()));

        if (settings.addNotesAfterHold()) {
            notes.add(new Note(start + settings.holdDuration() + settings.step() * 0.5, mirroredLane(lane)));
        }

        return lane;
    }

    private int nextLane(int previousLane) {
        int lane = RANDOM.nextInt(4);

        if (lane == previousLane) {
            lane = (lane + 1 + RANDOM.nextInt(3)) % 4;
        }

        return lane;
    }

    private int mirroredLane(int lane) {
        return switch (lane) {
            case 0 -> 3;
            case 1 -> 2;
            case 2 -> 1;
            default -> 0;
        };
    }

    private record ChartSettings(
            double durationSeconds,
            double firstNoteTime,
            double step,
            double holdDuration,
            int restEveryPhrases,
            double restLength,
            int holdEveryPhrases,
            int patternTypes,
            boolean addNotesAfterHold
    ) {
        private static ChartSettings forStage(PlayableStage stage) {
            if (stage instanceof WaveStage waveStage) {
                return forWaveStage(waveStage);
            }

            if (stage instanceof LittleBellStage littleBellStage) {
                return forLittleBellStage(littleBellStage);
            }

            throw new IllegalArgumentException("Unknown playable stage: " + stage);
        }

        private static ChartSettings forWaveStage(WaveStage stage) {
            return switch (stage) {
                case DRIFT -> new ChartSettings(56.25, 1.50, 0.75, 1.50, 6, 1.50, 7, 2, false);
                case FIRST_WAVES -> new ChartSettings(75.5, 1.00, 0.62, 1.35, 5, 0.9, 5, 3, false);
                case STRUGGLE -> new ChartSettings(45, 1.00, 0.54, 1.45, 6, 0.75, 5, 4, true);
                case STORM -> new ChartSettings(50, 1.00, 0.48, 1.55, 7, 0.65, 4, 4, true);
                case BEYOND_THE_ISLAND -> new ChartSettings(55, 1.00, 0.42, 1.65, 8, 0.55, 4, 4, true);
                case ONE_PULL_ONE_RELEASE -> new ChartSettings(60, 1.00, 0.40, 1.75, 8, 0.55, 4, 4, true);
            };
        }

        private static ChartSettings forLittleBellStage(LittleBellStage stage) {
            return switch (stage) {
                case EMPTY_BASKET -> new ChartSettings(34, 1.00, 0.74, 1.20, 4, 1.15, 5, 2, false);
                case UNDER_THE_TABLE -> new ChartSettings(38, 1.00, 0.64, 1.25, 5, 0.95, 5, 3, false);
                case RAIN_ALLEY -> new ChartSettings(43, 1.00, 0.56, 1.45, 6, 0.80, 4, 3, true);
                case WINDOW_LIGHT -> new ChartSettings(47, 1.00, 0.50, 1.55, 6, 0.70, 4, 4, true);
                case LITTLE_BELL -> new ChartSettings(52, 1.00, 0.46, 1.70, 7, 0.62, 4, 4, true);
            };
        }
    }
}
