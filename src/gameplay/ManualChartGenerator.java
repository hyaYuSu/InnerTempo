package gameplay;

import gameplay.stageCharts.DriftManualChart;
import gameplay.stageCharts.FirstWavesManualChart;
import gameplay.stageCharts.OnePullOneReleaseManualChart;
import gameplay.stageCharts.PathOfLightManualChart;
import gameplay.stageCharts.SteadyRhythmManualChart;
import gameplay.stageCharts.StruggleManualChart;
import model.Note;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManualChartGenerator {
    private final Map<PlayableStage, ManualStageChart> charts = Map.ofEntries(
            Map.entry(WaveStage.DRIFT, new DriftManualChart()),
            Map.entry(WaveStage.FIRST_WAVES, new FirstWavesManualChart()),
            Map.entry(WaveStage.STRUGGLE, new StruggleManualChart()),
            Map.entry(WaveStage.STORM, new SteadyRhythmManualChart()),
            Map.entry(WaveStage.BEYOND_THE_ISLAND, new PathOfLightManualChart()),
            Map.entry(WaveStage.ONE_PULL_ONE_RELEASE, new OnePullOneReleaseManualChart()),
            Map.entry(LittleBellStage.EMPTY_BASKET, new DriftManualChart()),
            Map.entry(LittleBellStage.UNDER_THE_TABLE, new FirstWavesManualChart()),
            Map.entry(LittleBellStage.RAIN_ALLEY, new StruggleManualChart()),
            Map.entry(LittleBellStage.WINDOW_LIGHT, new SteadyRhythmManualChart()),
            Map.entry(LittleBellStage.THRESHOLD_LIGHT, new PathOfLightManualChart()),
            Map.entry(LittleBellStage.LITTLE_BELL, new OnePullOneReleaseManualChart())
    );

    public Optional<List<Note>> generate(PlayableStage stage) {
        ManualStageChart chart = charts.get(stage);

        if (chart == null) {
            return Optional.empty();
        }

        return Optional.of(chart.createNotes());
    }
}
