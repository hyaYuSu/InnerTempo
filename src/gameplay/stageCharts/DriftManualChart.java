package gameplay.stageCharts;

import gameplay.ChartBuilder;
import gameplay.ManualStageChart;
import gameplay.StageBeatProfile;
import model.Note;

import java.util.List;

public class DriftManualChart implements ManualStageChart {
    @Override
    public List<Note> createNotes() {
        ChartBuilder chart = new ChartBuilder(StageBeatProfile.drift());

        chart.taps(0, new int[] {8, 12}, new int[] {0, 1});
        chart.taps(1, new int[] {0, 4, 8, 12}, new int[] {0, 1, 2, 3});
        chart.taps(2, new int[] {0, 4, 8, 10, 12}, new int[] {3, 2, 1, 2, 0});
        chart.hold(3, 0, 1, 8);
        chart.taps(3, new int[] {10}, new int[] {2});
        chart.gold(3, 12, 3);

        chart.taps(4, new int[] {0, 4, 8, 12}, new int[] {0, 2, 1, 3});
        chart.taps(5, new int[] {0, 6, 8, 12}, new int[] {3, 1, 2, 0});
        chart.hold(6, 0, 2, 8);
        chart.taps(6, new int[] {8, 12, 14}, new int[] {0, 1, 2});
        chart.taps(7, new int[] {0, 4, 8}, new int[] {3, 2, 1});
        chart.gold(7, 12, 0);

        chart.taps(8, new int[] {0, 4, 8, 12}, new int[] {0, 1, 2, 3});
        chart.taps(9, new int[] {0, 4, 6, 8, 12}, new int[] {3, 1, 2, 1, 0});
        chart.hold(10, 0, 0, 8);
        chart.taps(10, new int[] {8, 12}, new int[] {2, 3});
        chart.taps(11, new int[] {0, 4, 8, 10, 14}, new int[] {3, 2, 1, 2, 1});
        chart.gold(11, 12, 0);

        chart.taps(12, new int[] {0, 4, 8, 12}, new int[] {0, 1, 2, 3});
        chart.hold(13, 0, 3, 8);
        chart.taps(13, new int[] {8, 10, 12}, new int[] {1, 2, 0});
        chart.taps(14, new int[] {0, 4, 8, 12}, new int[] {0, 2, 1, 3});
        chart.taps(15, new int[] {0, 4, 6, 8, 14}, new int[] {3, 2, 1, 2, 1});
        chart.gold(15, 12, 0);

        chart.taps(16, new int[] {0, 4, 8, 12}, new int[] {0, 1, 2, 3});
        chart.hold(17, 0, 1, 8);
        chart.taps(17, new int[] {8, 12}, new int[] {2, 3});
        chart.taps(18, new int[] {0, 4, 8, 10, 12}, new int[] {3, 2, 1, 2, 0});
        chart.taps(19, new int[] {0}, new int[] {1});
        chart.gold(19, 4, 0);

        return chart.notes();
    }
}
