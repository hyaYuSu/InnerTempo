package gameplay.stageCharts;

import gameplay.ChartBuilder;
import gameplay.ManualStageChart;
import gameplay.StageBeatProfile;
import model.Note;

import java.util.List;

public class OnePullOneReleaseManualChart implements ManualStageChart {
    private static final int TOTAL_MEASURES = 22;

    @Override
    public List<Note> createNotes() {
        ChartBuilder chart = new ChartBuilder(StageBeatProfile.pathOfLight());

        for (int measure = 0; measure < TOTAL_MEASURES; measure++) {
            if (measure < 6) {
                addBreath(chart, measure);
            } else if (measure < 15) {
                addRowingPattern(chart, measure);
            } else {
                addClosingPattern(chart, measure);
            }
        }

        return chart.notes();
    }

    private void addBreath(ChartBuilder chart, int measure) {
        switch (measure % 3) {
            case 0 -> chart.taps(measure, new int[] {8, 12}, chart.lanesFor(measure, 2, 3));
            case 1 -> chart.taps(measure, new int[] {0, 4, 8}, chart.lanesFor(measure, 1, 2, 3));
            default -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 8);
                chart.taps(measure, new int[] {8, 12}, chart.lanesFor(measure, 3, 0));
            }
        }
    }

    private void addRowingPattern(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 0, 1, 2, 3));
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 3), 8);
                chart.taps(measure, new int[] {8, 12}, chart.lanesFor(measure, 1, 0));
            }
            case 2 -> chart.taps(measure, new int[] {0, 8, 12}, chart.lanesFor(measure, 2, 1, 3));
            default -> {
                chart.taps(measure, new int[] {0, 4, 8}, chart.lanesFor(measure, 3, 2, 1));
                chart.gold(measure, 12, chart.laneFor(measure, 0));
            }
        }
    }

    private void addClosingPattern(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {0, 8}, chart.lanesFor(measure, 0, 2));
            case 1 -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 1, 2, 3, 2));
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 0), 12);
                chart.taps(measure, new int[] {12}, chart.lanesFor(measure, 3));
            }
            default -> {
                chart.taps(measure, new int[] {0, 8}, chart.lanesFor(measure, 2, 1));
                chart.gold(measure, 12, chart.laneFor(measure, 0));
            }
        }
    }
}
