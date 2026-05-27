package gameplay.stageCharts;

import gameplay.ChartBuilder;
import gameplay.ManualStageChart;
import gameplay.StageBeatProfile;
import model.Note;

import java.util.List;

public class SteadyRhythmManualChart implements ManualStageChart {
    private static final int TOTAL_MEASURES = 22;

    @Override
    public List<Note> createNotes() {
        ChartBuilder chart = new ChartBuilder(StageBeatProfile.steadyRhythm());

        for (int measure = 0; measure < TOTAL_MEASURES; measure++) {
            if (measure < 4) {
                addBreathingPattern(chart, measure);
            } else if (measure < 12) {
                addSteadyPattern(chart, measure);
            } else if (measure < 18) {
                addMovingPattern(chart, measure);
            } else {
                addResolvePattern(chart, measure);
            }
        }

        return chart.notes();
    }

    private void addBreathingPattern(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {0, 8}, chart.lanesFor(measure, 0, 2));
            case 1 -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 1, 2, 3, 2));
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 0), 8);
                chart.taps(measure, new int[] {8, 12}, chart.lanesFor(measure, 2, 3));
            }
            default -> {
                chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 3, 2, 1, 0));
                chart.gold(measure, 14, chart.laneFor(measure, 1));
            }
        }
    }

    private void addSteadyPattern(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 0, 1, 2, 3));
            case 1 -> chart.taps(measure, new int[] {0, 6, 8, 12}, chart.lanesFor(measure, 3, 1, 2, 0));
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 8);
                chart.taps(measure, new int[] {8, 10, 12}, chart.lanesFor(measure, 3, 2, 0));
            }
            default -> {
                chart.taps(measure, new int[] {0, 4, 8, 10, 12}, chart.lanesFor(measure, 2, 0, 1, 2, 3));
                chart.gold(measure, 14, chart.laneFor(measure, 0));
            }
        }
    }

    private void addMovingPattern(ChartBuilder chart, int measure) {
        switch (measure % 3) {
            case 0 -> chart.taps(measure, new int[] {0, 4, 6, 8, 12}, chart.lanesFor(measure, 0, 1, 2, 1, 3));
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 2), 10);
                chart.taps(measure, new int[] {10, 12, 14}, chart.lanesFor(measure, 0, 1, 3));
            }
            default -> {
                chart.taps(measure, new int[] {0, 4, 8, 10, 12, 14}, chart.lanesFor(measure, 3, 2, 1, 2, 0, 1));
                chart.gold(measure, 6, chart.laneFor(measure, 0));
            }
        }
    }

    private void addResolvePattern(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 12);
                chart.taps(measure, new int[] {12}, chart.lanesFor(measure, 3));
            }
            case 3 -> {
                chart.taps(measure, new int[] {0, 4, 8}, chart.lanesFor(measure, 0, 2, 1));
                chart.gold(measure, 12, chart.laneFor(measure, 3));
            }
            default -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 3, 2, 1, 0));
        }
    }
}
