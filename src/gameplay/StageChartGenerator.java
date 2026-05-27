package gameplay;

import model.Stages.LittleBellStage;
import model.Note;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class StageChartGenerator {
    private static final double MUSIC_END_PADDING_SECONDS = 0.75;

    private final ManualChartGenerator manualChartGenerator;

    public StageChartGenerator() {
        this.manualChartGenerator = new ManualChartGenerator();
    }

    public List<Note> generateChart(PlayableStage stage) {
        return generateChart(stage, 0);
    }

    public List<Note> generateChart(PlayableStage stage, double targetDurationSeconds) {
        return manualChartGenerator.generate(stage)
                .map(notes -> trimToDuration(polishChart(notes), targetDurationSeconds))
                .orElseGet(() -> trimToDuration(
                        polishChart(generate(ChartSettings.forStage(stage, targetDurationSeconds))),
                        targetDurationSeconds
                ));
    }

    private List<Note> polishChart(List<Note> sourceNotes) {
        List<Note> sortedNotes = new ArrayList<>(sourceNotes);
        sortedNotes.sort(Comparator
                .comparingDouble(Note::getTime)
                .thenComparingInt(Note::getLane));

        List<Note> playableNotes = new ArrayList<>();
        double[] laneBlockedUntil = {-1, -1, -1, -1};

        for (Note note : sortedNotes) {
            int lane = note.getLane();
            if (lane < 0 || lane >= laneBlockedUntil.length) {
                continue;
            }

            if (note.getTime() < laneBlockedUntil[lane] - 0.05) {
                continue;
            }

            playableNotes.add(note);
            if (note.isHoldNote()) {
                laneBlockedUntil[lane] = Math.max(laneBlockedUntil[lane], note.getHoldEndTime());
            }
        }

        return playableNotes;
    }

    private List<Note> trimToDuration(List<Note> notes, double targetDurationSeconds) {
        if (targetDurationSeconds <= 0) {
            return notes;
        }

        double latestEndTime = Math.max(0, targetDurationSeconds - MUSIC_END_PADDING_SECONDS);
        List<Note> trimmedNotes = new ArrayList<>();

        for (Note note : notes) {
            if (note.getHoldEndTime() <= latestEndTime) {
                trimmedNotes.add(note);
            }
        }

        return trimmedNotes;
    }

    private List<Note> generate(ChartSettings settings) {
        List<Note> notes = new ArrayList<>();
        Random random = new Random(settings.seed());

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
                previousLane = addHoldPattern(notes, time, settings, previousLane, random);
                time += settings.holdDuration() + settings.step();
                phrase++;
                continue;
            }

            int pattern = random.nextInt(settings.patternTypes());

            if (pattern == 0) {
                previousLane = addAlternatingPattern(notes, time, settings, previousLane, random);
                time += settings.step() * 4;
            } else if (pattern == 1) {
                previousLane = addWalkPattern(notes, time, settings, previousLane, random);
                time += settings.step() * 4;
            } else if (pattern == 2) {
                previousLane = addBurstPattern(notes, time, settings, previousLane, random);
                time += settings.step() * 3;
            } else {
                previousLane = addHoldPattern(notes, time, settings, previousLane, random);
                time += settings.holdDuration() + settings.step();
            }

            phrase++;
        }

        return notes;
    }

    private int addAlternatingPattern(List<Note> notes, double start, ChartSettings settings, int previousLane, Random random) {
        int firstLane = nextLane(previousLane, random);
        int secondLane = mirroredLane(firstLane);

        notes.add(new Note(start, firstLane));
        notes.add(new Note(start + settings.step(), secondLane));
        notes.add(new Note(start + settings.step() * 2, firstLane));
        notes.add(new Note(start + settings.step() * 3, secondLane));

        return secondLane;
    }

    private int addWalkPattern(List<Note> notes, double start, ChartSettings settings, int previousLane, Random random) {
        int lane = nextLane(previousLane, random);
        int direction = random.nextBoolean() ? 1 : -1;

        for (int i = 0; i < 4; i++) {
            notes.add(new Note(start + settings.step() * i, lane));
            lane = Math.floorMod(lane + direction, 4);
        }

        return Math.floorMod(lane - direction, 4);
    }

    private int addBurstPattern(List<Note> notes, double start, ChartSettings settings, int previousLane, Random random) {
        int lane = nextLane(previousLane, random);
        double burstStep = settings.step() * 0.55;

        for (int i = 0; i < 3; i++) {
            notes.add(new Note(start + burstStep * i, lane));
            lane = nextLane(lane, random);
        }

        return lane;
    }

    private int addHoldPattern(List<Note> notes, double start, ChartSettings settings, int previousLane, Random random) {
        int lane = nextLane(previousLane, random);
        notes.add(new Note(start, lane, settings.holdDuration()));

        if (settings.addNotesAfterHold()) {
            notes.add(new Note(start + settings.holdDuration() + settings.step() * 0.5, mirroredLane(lane)));
        }

        return lane;
    }

    private int nextLane(int previousLane, Random random) {
        int lane = random.nextInt(4);

        if (lane == previousLane) {
            lane = (lane + 1 + random.nextInt(3)) % 4;
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
            boolean addNotesAfterHold,
            long seed
    ) {
        private static ChartSettings forStage(PlayableStage stage, double targetDurationSeconds) {
            if (stage instanceof WaveStage waveStage) {
                return forWaveStage(waveStage, targetDurationSeconds);
            }

            if (stage instanceof LittleBellStage littleBellStage) {
                return forLittleBellStage(littleBellStage, targetDurationSeconds);
            }

            throw new IllegalArgumentException("Unknown playable stage: " + stage);
        }

        private static ChartSettings forWaveStage(WaveStage stage, double targetDurationSeconds) {
            return switch (stage) {
                case DRIFT -> new ChartSettings(56.25, 1.50, 0.75, 1.50, 6, 1.50, 7, 2, false, 101);
                case FIRST_WAVES -> {
                    StageBeatProfile timing = StageBeatProfile.firstWaves();
                    yield new ChartSettings(
                            55.5,
                            timing.timeForGridSlot(0, 8),
                            timing.gridSlotSeconds() * 2,
                            timing.beatSeconds() * 2,
                            6,
                            timing.beatSeconds(),
                            5,
                            4,
                            true,
                            102
                    );
                }
                case STRUGGLE -> new ChartSettings(75.5, 1.00, 0.62, 1.35, 5, 0.9, 5, 3, false, 103);
                case STORM -> {
                    StageBeatProfile timing = StageBeatProfile.steadyRhythm();
                    yield new ChartSettings(
                            playableDuration(targetDurationSeconds, 50),
                            timing.timeForGridSlot(0, 4),
                            timing.beatSeconds(),
                            timing.beatSeconds() * 2,
                            7,
                            timing.beatSeconds() * 2,
                            4,
                            4,
                            true,
                            104
                    );
                }
                case BEYOND_THE_ISLAND -> {
                    StageBeatProfile timing = StageBeatProfile.pathOfLight();
                    yield new ChartSettings(
                            playableDuration(targetDurationSeconds, 55),
                            timing.timeForGridSlot(0, 4),
                            timing.gridSlotSeconds() * 2,
                            timing.beatSeconds() * 2,
                            7,
                            timing.beatSeconds(),
                            4,
                            4,
                            true,
                            105
                    );
                }
                case ONE_PULL_ONE_RELEASE -> {
                    StageBeatProfile timing = StageBeatProfile.pathOfLight();
                    yield new ChartSettings(
                            playableDuration(targetDurationSeconds, 55),
                            timing.timeForGridSlot(0, 8),
                            timing.beatSeconds(),
                            timing.beatSeconds() * 2,
                            7,
                            timing.beatSeconds() * 2,
                            6,
                            2,
                            false,
                            106
                    );
                }
            };
        }

        private static double playableDuration(double targetDurationSeconds, double fallbackDurationSeconds) {
            if (targetDurationSeconds <= 0) {
                return fallbackDurationSeconds;
            }

            return Math.max(8, targetDurationSeconds - 2.0);
        }

        private static ChartSettings forLittleBellStage(LittleBellStage stage, double targetDurationSeconds) {
            return switch (stage) {
                case EMPTY_BASKET -> forWaveStage(WaveStage.DRIFT, targetDurationSeconds);
                case UNDER_THE_TABLE -> forWaveStage(WaveStage.FIRST_WAVES, targetDurationSeconds);
                case RAIN_ALLEY -> forWaveStage(WaveStage.STRUGGLE, targetDurationSeconds);
                case WINDOW_LIGHT -> forWaveStage(WaveStage.STORM, targetDurationSeconds);
                case THRESHOLD_LIGHT -> forWaveStage(WaveStage.BEYOND_THE_ISLAND, targetDurationSeconds);
                case LITTLE_BELL -> forWaveStage(WaveStage.ONE_PULL_ONE_RELEASE, targetDurationSeconds);
            };
        }
    }
}
