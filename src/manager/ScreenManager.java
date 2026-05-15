package manager;

import config.GameConfig;
import gameplay.RandomChartGenerator;
import model.Journey;
import model.JourneyId;
import model.JourneyScene;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import screens.*;
import settings.GameplaySettings;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

public class ScreenManager {
    private final JFrame frame;
    private final GameplaySettings options;
    private final SaveManager saveManager;
    private final RandomChartGenerator chartGenerator;

    public ScreenManager(JFrame frame) {
        this.frame = frame;
        this.options = new GameplaySettings();
        this.saveManager = new SaveManager();
        this.chartGenerator = new RandomChartGenerator();
    }

    public void showTitle() {
        setScreen(new TitleScreen(this).create());
    }

    public void showMainMenu() {
        setScreen(new MainMenuScreen(this).create());
    }

    public void showJourneySelect() {
        showJourneySelect(JourneyId.WAVES);
    }

    public void showJourneySelect(JourneyId selectedJourney) {
        setScreen(new JourneySelectScreen(this, saveManager, selectedJourney).create());
    }

    public void showJourneyScene(Journey journey, JourneyScene scene) {
        setScreen(new JourneyScenePlaceholderScreen(this, journey, scene).create());
    }

    public void showOptions() {
        setScreen(new OptionsScreen(this, options).create());
    }

    public void startStage(PlayableStage playableStage) {
        setScreen(new RhythmGameScreen(this, options, chartGenerator, playableStage).create());
    }

    public void showLore(PlayableStage playableStage) {
        setScreen(new LoreScreen(this, playableStage).create());
    }

    public void showResults(PlayableStage playableStage, ScoreTracker scoreTracker) {
        setScreen(new ResultsScreen(this, saveManager, playableStage, scoreTracker).create());
    }

    public void showStageResultStory(PlayableStage playableStage, ScoreTracker scoreTracker) {
        setScreen(new StageResultStoryScreen(this, playableStage, scoreTracker).create());
    }

    private void setScreen(JPanel screen) {
        screen.setPreferredSize(new Dimension(GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT));
        frame.setContentPane(screen);
        frame.pack();
        frame.revalidate();
        frame.repaint();
        SwingUtilities.invokeLater(screen::requestFocusInWindow);
    }
}
