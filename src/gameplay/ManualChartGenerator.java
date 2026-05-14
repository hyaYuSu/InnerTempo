package gameplay;

import gameplay.stageCharts.DriftManualChart;
import gameplay.stageCharts.FirstWavesManualChart;
import model.Note;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManualChartGenerator {
    private final Map<PlayableStage, ManualStageChart> charts = Map.of(
            WaveStage.DRIFT, new DriftManualChart(),
            WaveStage.FIRST_WAVES, new FirstWavesManualChart()
    );

    public Optional<List<Note>> generate(PlayableStage stage) {
        ManualStageChart chart = charts.get(stage);

        if (chart == null) {
            return Optional.empty();
        }

        return Optional.of(chart.createNotes());
    }
}
