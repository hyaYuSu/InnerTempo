package gameplay.stageCharts;

import gameplay.ChartBuilder;
import gameplay.ManualStageChart;
import gameplay.StageBeatProfile;
import model.Note;

import java.util.List;

public class PathOfLightManualChart implements ManualStageChart {
    private static final int TOTAL_MEASURES = 22;

    @Override
    public List<Note> createNotes() {
        ChartBuilder chart = new ChartBuilder(StageBeatProfile.pathOfLight());

        for (int measure = 0; measure < TOTAL_MEASURES; measure++) {
            if (measure < 4) {
                addEntrance(chart, measure);
            } else if (measure < 11) {
                addLightPath(chart, measure);
            } else if (measure < 18) {
                addBrightCurrent(chart, measure);
            } else {
                addFinalPush(chart, measure);
            }
        }

        return chart.notes();
    }

    private void addEntrance(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {8, 12}, chart.lanesFor(measure, 2, 3));
            case 1 -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 3, 1, 2, 0));
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 8);
                chart.taps(measure, new int[] {8, 10, 12}, chart.lanesFor(measure, 3, 2, 0));
            }
            default -> {
                chart.taps(measure, new int[] {0, 4, 8, 10, 12}, chart.lanesFor(measure, 0, 1, 3, 2, 1));
                chart.gold(measure, 14, chart.laneFor(measure, 3));
            }
        }
    }

    private void addLightPath(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(
                    measure,
                    new int[] {0, 2, 4, 8, 10, 12},
                    chart.lanesFor(measure, 0, 1, 2, 3, 2, 1)
            );
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 3), 8);
                chart.taps(
                        measure,
                        new int[] {8, 10, 12, 14},
                        chart.lanesFor(measure, 0, 1, 2, 1)
                );
            }
            case 2 -> {
                chart.taps(
                        measure,
                        new int[] {0, 4, 6, 8, 10, 12, 14},
                        chart.lanesFor(measure, 3, 1, 2, 0, 1, 2, 3)
                );
                chart.gold(measure, 6, chart.laneFor(measure, 0));
            }
            default -> chart.taps(
                    measure,
                    new int[] {0, 2, 4, 6, 8, 12},
                    chart.lanesFor(measure, 1, 2, 3, 2, 0, 1)
            );
        }
    }

    private void addBrightCurrent(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(
                    measure,
                    new int[] {0, 2, 4, 6, 8, 10, 12, 14},
                    chart.lanesFor(measure, 0, 1, 2, 3, 2, 1, 0, 3)
            );
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 8);
                chart.taps(
                        measure,
                        new int[] {8, 10, 12, 14},
                        chart.lanesFor(measure, 3, 2, 0, 1)
                );
            }
            case 2 -> {
                chart.taps(
                        measure,
                        new int[] {0, 4, 6, 8, 10, 12, 14},
                        chart.lanesFor(measure, 3, 2, 1, 0, 1, 2, 3)
                );
                chart.gold(measure, 12, chart.laneFor(measure, 0));
            }
            default -> chart.taps(
                    measure,
                    new int[] {0, 2, 4, 8, 10, 12, 14},
                    chart.lanesFor(measure, 0, 2, 1, 3, 2, 1, 0)
            );
        }
    }

    private void addFinalPush(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 2), 12);
                chart.taps(measure, new int[] {12, 14}, chart.lanesFor(measure, 0, 1));
            }
            case 3 -> {
                chart.taps(
                        measure,
                        new int[] {0, 4, 8, 10, 12, 14},
                        chart.lanesFor(measure, 3, 1, 2, 0, 1, 3)
                );
                chart.gold(measure, 6, chart.laneFor(measure, 2));
            }
            default -> chart.taps(
                    measure,
                    new int[] {0, 2, 4, 8, 12},
                    chart.lanesFor(measure, 0, 1, 2, 3, 1)
            );
        }
    }
}
