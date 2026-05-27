package gameplay.stageCharts;

import gameplay.ChartBuilder;
import gameplay.ManualStageChart;
import gameplay.StageBeatProfile;
import model.Note;

import java.util.List;

public class StruggleManualChart implements ManualStageChart {
    private static final int TOTAL_MEASURES = 25;

    @Override
    public List<Note> createNotes() {
        ChartBuilder chart = new ChartBuilder(StageBeatProfile.struggle());

        for (int measure = 0; measure < TOTAL_MEASURES; measure++) {
            if (measure < 4) {
                addRisingWater(chart, measure);
            } else if (measure < 10) {
                addRoughPattern(chart, measure);
            } else if (measure < 15) {
                addStormPattern(chart, measure);
            } else {
                addHoldOnPattern(chart, measure);
            }
        }

        return chart.notes();
    }

    private void addRisingWater(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {8, 12}, chart.lanesFor(measure, 0, 1));
            case 1 -> chart.taps(measure, new int[] {0, 8, 12}, chart.lanesFor(measure, 2, 1, 3));
            case 2 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 8);
                chart.taps(measure, new int[] {10, 14}, chart.lanesFor(measure, 2, 0));
            }
            default -> {
                chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 3, 2, 1, 0));
                chart.gold(measure, 14, chart.laneFor(measure, 2));
            }
        }
    }

    private void addRoughPattern(ChartBuilder chart, int measure) {
        switch (measure % 3) {
            case 0 -> chart.taps(measure, new int[] {0, 4, 8, 10, 12}, chart.lanesFor(measure, 0, 2, 1, 3, 2));
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 3), 8);
                chart.taps(measure, new int[] {8, 12, 14}, chart.lanesFor(measure, 0, 1, 2));
            }
            default -> {
                chart.taps(measure, new int[] {0, 3, 6, 8, 12}, chart.lanesFor(measure, 3, 1, 2, 0, 1));
                chart.gold(measure, 10, chart.laneFor(measure, 3));
            }
        }
    }

    private void addStormPattern(ChartBuilder chart, int measure) {
        switch (measure % 4) {
            case 0 -> chart.taps(measure, new int[] {0, 4, 6, 8, 12, 14}, chart.lanesFor(measure, 0, 1, 3, 2, 1, 0));
            case 1 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 2), 10);
                chart.taps(measure, new int[] {10, 12, 14}, chart.lanesFor(measure, 3, 1, 0));
            }
            case 2 -> chart.taps(measure, new int[] {0, 2, 4, 8, 10, 12}, chart.lanesFor(measure, 3, 2, 1, 0, 2, 1));
            default -> {
                chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 1, 3, 0, 2));
                chart.gold(measure, 14, chart.laneFor(measure, 0));
            }
        }
    }

    private void addHoldOnPattern(ChartBuilder chart, int measure) {
        switch (measure % 3) {
            case 0 -> {
                chart.hold(measure, 0, chart.laneFor(measure, 1), 12);
                chart.taps(measure, new int[] {12}, chart.lanesFor(measure, 3));
            }
            case 1 -> chart.taps(measure, new int[] {0, 4, 8, 12}, chart.lanesFor(measure, 0, 1, 2, 3));
            default -> {
                chart.taps(measure, new int[] {0, 8, 12}, chart.lanesFor(measure, 3, 1, 0));
                chart.gold(measure, 14, chart.laneFor(measure, 2));
            }
        }
    }
}
