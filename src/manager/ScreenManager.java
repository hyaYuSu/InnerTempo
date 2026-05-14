package manager;

import gameplay.RandomChartGenerator;
import model.Journey;
import model.JourneyId;
import model.JourneyScene;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import screens.*;
import settings.GameplaySettings;

import javafx.stage.Stage;

public class ScreenManager {
    private final Stage stage;
    private final GameplaySettings options;
    private final SaveManager saveManager;
    private final RandomChartGenerator chartGenerator;

    public ScreenManager(Stage stage) {
        this.stage = stage;
        this.options = new GameplaySettings();
        this.saveManager = new SaveManager();
        this.chartGenerator = new RandomChartGenerator();
    }

    public void showTitle() {
        stage.setScene(new TitleScreen(this).create());
    }

    public void showMainMenu() {
        stage.setScene(new MainMenuScreen(this).create());
    }

    public void showJourneySelect() {
        showJourneySelect(JourneyId.WAVES);
    }

    public void showJourneySelect(JourneyId selectedJourney) {
        stage.setScene(new JourneySelectScreen(this, saveManager, selectedJourney).create());
    }

    public void showJourneyScene(Journey journey, JourneyScene scene) {
        stage.setScene(new JourneyScenePlaceholderScreen(this, journey, scene).create());
    }

    public void showOptions() {
        stage.setScene(new OptionsScreen(this, options).create());
    }

    public void startStage(PlayableStage playableStage) {
        stage.setScene(new RhythmGameScreen(this, options, chartGenerator, playableStage).create());
    }

    public void showLore(PlayableStage playableStage) {
        stage.setScene(new LoreScreen(this, playableStage).create());
    }

    public void showResults(PlayableStage playableStage, ScoreTracker scoreTracker) {
        stage.setScene(new ResultsScreen(this, saveManager, playableStage, scoreTracker).create());
    }

    public void showStageResultStory(PlayableStage playableStage, ScoreTracker scoreTracker) {
        stage.setScene(new StageResultStoryScreen(this, playableStage, scoreTracker).create());
    }
}
