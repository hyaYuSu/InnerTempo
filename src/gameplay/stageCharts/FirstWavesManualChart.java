package gameplay.stageCharts;

import gameplay.ChartBuilder;
import gameplay.ManualStageChart;
import gameplay.StageBeatProfile;
import model.Note;

import java.util.List;

public class FirstWavesManualChart implements ManualStageChart {
    private static final int PLAYABLE_START_MEASURE = 2;
    private static final int MAIN_ENTRANCE_MEASURE = 8;
    private static final int TOTAL_MEASURES = 32;

    @Override
    public List<Note> createNotes() {
        ChartBuilder chart = new ChartBuilder(StageBeatProfile.firstWaves());

        for (int measure = PLAYABLE_START_MEASURE; measure < TOTAL_MEASURES; measure++) {
            if (measure < MAIN_ENTRANCE_MEASURE) {
                addLeadIn(chart, measure);
            } else if (measure == MAIN_ENTRANCE_MEASURE) {
                addEntrance(chart, measure);
            } else if (measure < 16) {
                addBuild(chart, measure);
            } else if (measure < 24) {
                addLift(chart, measure);
            } else {
                addFinale(chart, measure);
            }
        }

        return chart.notes();
    }

    private void addLeadIn(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(
                    measure,
                    new int[] {0, 16},
                    chart.lanesFor(measure, 0, 2)
            );
            case 1 -> chart.taps(
                    measure,
                    new int[] {0, 8, 16},
                    chart.lanesFor(measure, 3, 1, 2)
            );
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 12);
                chart.taps(
                        measure,
                        new int[] {16, 24},
                        chart.lanesFor(measure, 2, 3)
                );
            }
            default -> {
                chart.taps(
                        measure,
                        new int[] {0, 8, 16, 22},
                        chart.lanesFor(measure, 0, 1, 3, 2)
                );
                chart.gold(measure, 24, chart.laneFor(measure, 3));
            }
        }
    }

    private void addEntrance(ChartBuilder chart, int measure) {
        chart.taps(
                measure,
                new int[] {0, 8, 16, 24},
                chart.lanesFor(measure, 0, 1, 2, 3)
        );
        chart.gold(measure, 28, chart.laneFor(measure, 1));
    }

    private void addBuild(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(
                    measure,
                    new int[] {0, 8, 16, 20, 24},
                    chart.lanesFor(measure, 0, 1, 2, 1, 3)
            );
            case 1 -> {
                chart.taps(
                        measure,
                        new int[] {0, 6, 8, 16, 24},
                        chart.lanesFor(measure, 3, 2, 1, 0, 2)
                );
                chart.gold(measure, 28, chart.laneFor(measure, 1));
            }
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 2), 16);
                chart.taps(
                        measure,
                        new int[] {18, 24, 28},
                        chart.lanesFor(measure, 0, 1, 3)
                );
            }
            default -> chart.taps(
                    measure,
                    new int[] {0, 8, 12, 16, 20, 24},
                    chart.lanesFor(measure, 1, 3, 2, 0, 2, 3)
            );
        }
    }

    private void addLift(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(
                    measure,
                    new int[] {0, 4, 8, 16, 24, 28},
                    chart.lanesFor(measure, 0, 1, 2, 3, 2, 0)
            );
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 3), 12);
                chart.taps(
                        measure,
                        new int[] {12, 16, 22, 24},
                        chart.lanesFor(measure, 0, 1, 2, 1)
                );
            }
            case 2 -> {
                chart.taps(
                        measure,
                        new int[] {0, 4, 8, 14, 16, 24, 28},
                        chart.lanesFor(measure, 3, 2, 1, 0, 1, 2, 3)
                );
                chart.gold(measure, 20, chart.laneFor(measure, 0));
            }
            default -> chart.taps(
                    measure,
                    new int[] {0, 8, 12, 16, 20, 24, 30},
                    chart.lanesFor(measure, 0, 2, 1, 3, 2, 1, 0)
            );
        }
    }

    private void addFinale(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> {
                chart.taps(
                        measure,
                        new int[] {0, 4, 8, 12, 16, 20, 24, 28},
                        chart.lanesFor(measure, 0, 1, 2, 3, 2, 1, 0, 3)
                );
                chart.gold(measure, 30, chart.laneFor(measure, 2));
            }
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 16);
                chart.taps(
                        measure,
                        new int[] {16, 20, 24, 28},
                        chart.lanesFor(measure, 3, 2, 0, 1)
                );
            }
            case 2 -> chart.taps(
                    measure,
                    new int[] {0, 6, 8, 12, 16, 22, 24, 28},
                    chart.lanesFor(measure, 3, 1, 2, 0, 1, 2, 3, 0)
            );
            default -> {
                chart.taps(
                        measure,
                        new int[] {0, 8, 12, 16, 20, 24},
                        chart.lanesFor(measure, 0, 2, 3, 1, 2, 0)
                );
                chart.gold(measure, 28, chart.laneFor(measure, 3));
            }
        }
    }
}
