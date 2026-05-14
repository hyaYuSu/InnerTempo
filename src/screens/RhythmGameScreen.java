package screens;

import audio.StageMusicPlayer;
import config.GameConfig;
import gameplay.HitJudge;
import gameplay.RandomChartGenerator;
import gameplay.StageBeatProfile;
import manager.ScreenManager;
import model.Note;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import settings.GameplaySettings;
import ui.GameplayFeedback;
import ui.GameUiFactory;
import ui.JudgmentStyle;
import ui.JourneyTheme;
import ui.NoteRenderer;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RhythmGameScreen {
    private static final double COUNTDOWN_SECONDS = 3.0;
    private static final double HOLD_TICK_INTERVAL = 0.25;
    private static final double HOLD_RELEASE_GRACE = 0.12;
    private static final double HIT_Y = 500;
    private static final double PLAYFIELD_LEFT = 220;
    private static final double PLAYFIELD_RIGHT = 780;
    private static final double[] LANE_X = {238, 378, 518, 658};

    private final ScreenManager controller;
    private final GameplaySettings options;
    private final RandomChartGenerator chartGenerator;
    private final PlayableStage playableStage;
    private final HitJudge hitJudge;
    private final JourneyTheme theme;
    private final StageBeatProfile beatProfile;

    private final List<Group> activeNotes = new ArrayList<>();
    private final boolean[] keyHeld = new boolean[4];
    private Group[] receptors;
    private Pane root;
    private VBox pauseOverlay;
    private Text countdownText;
    private Text pauseStatsText;
    private Text scoreText;
    private Text comboText;
    private Text accuracyText;
    private Text progressText;
    private Rectangle progressFill;
    private Text debugText;
    private Text judgmentText;
    private Text milestoneText;
    private ScoreTracker scoreTracker;
    private StageMusicPlayer musicPlayer;
    private long startTime;
    private long pauseStartedAt;
    private boolean paused;
    private boolean countdownComplete;
    private boolean musicStarted;
    private int lastBeatIndex = -1;
    private double chartEndTime;
    private AnimationTimer timer;

    public RhythmGameScreen(
            ScreenManager controller,
            GameplaySettings options,
            RandomChartGenerator chartGenerator,
            PlayableStage playableStage
    ) {
        this.controller = controller;
        this.options = options;
        this.chartGenerator = chartGenerator;
        this.playableStage = playableStage;
        this.hitJudge = new HitJudge(options);
        this.theme = JourneyTheme.forStage(playableStage);
        this.beatProfile = StageBeatProfile.forStage(playableStage).orElse(null);
    }

    public Scene create() {
        root = new Pane();
        root.setStyle(theme.getGameplayBackgroundStyle());

        List<Note> noteData = chartGenerator.generateChart(playableStage);
        scoreTracker = new ScoreTracker(noteData.size());
        musicPlayer = new StageMusicPlayer(playableStage);
        chartEndTime = getChartEndTime(noteData) + 0.6;

        createHud();
        createLaneGuides();
        createReceptors();
        createOverlays();

        Scene scene = new Scene(root, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
        startTime = System.nanoTime();
        timer = createTimer(noteData);

        scene.setOnKeyPressed(e -> handleKeyPressed(e.getCode()));
        scene.setOnKeyReleased(e -> handleKeyReleased(e.getCode()));

        timer.start();
        return scene;
    }

    private void createHud() {
        Text stageText = new Text("Stage " + playableStage.getNumber() + ": " + playableStage.getTitle());
        stageText.setFill(theme.getAccentColor());
        stageText.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        stageText.setTextAlignment(TextAlignment.CENTER);
        stageText.setWrappingWidth(GameConfig.SCENE_WIDTH);
        stageText.setX(0);
        stageText.setY(42);

        scoreText = new Text("Score 0");
        scoreText.setFill(theme.getSoftTextColor());
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        scoreText.setOpacity(0.72);
        scoreText.setTextAlignment(TextAlignment.RIGHT);
        scoreText.setWrappingWidth(150);
        scoreText.setX(820);
        scoreText.setY(42);

        comboText = new Text("Combo 0");
        comboText.setFill(Color.WHITE);
        comboText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        comboText.setX(32);
        comboText.setY(560);

        accuracyText = new Text("100%");
        accuracyText.setFill(theme.getSoftTextColor());
        accuracyText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        accuracyText.setTextAlignment(TextAlignment.RIGHT);
        accuracyText.setWrappingWidth(160);
        accuracyText.setX(808);
        accuracyText.setY(560);

        Rectangle progressBack = new Rectangle(330, 62, 340, 5);
        progressBack.setFill(Color.rgb(255, 255, 255, 0.18));
        progressBack.setArcWidth(8);
        progressBack.setArcHeight(8);

        progressFill = new Rectangle(330, 62, 0, 5);
        progressFill.setFill(theme.getAccentColor());
        progressFill.setArcWidth(8);
        progressFill.setArcHeight(8);

        progressText = new Text("0:00 / " + formatTime(chartEndTime));
        progressText.setFill(theme.getSoftTextColor());
        progressText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        progressText.setOpacity(0.78);
        progressText.setTextAlignment(TextAlignment.CENTER);
        progressText.setWrappingWidth(GameConfig.SCENE_WIDTH);
        progressText.setX(0);
        progressText.setY(85);

        debugText = new Text("");
        debugText.setFill(theme.getSoftTextColor());
        debugText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        debugText.setOpacity(0.78);
        debugText.setX(30);
        debugText.setY(585);
        debugText.setVisible(false);

        judgmentText = new Text("");
        judgmentText.setFill(Color.LIGHTCYAN);
        judgmentText.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        judgmentText.setTextAlignment(TextAlignment.CENTER);
        judgmentText.setWrappingWidth(GameConfig.SCENE_WIDTH);
        judgmentText.setX(0);
        judgmentText.setY(315);

        milestoneText = new Text("");
        milestoneText.setFill(Color.GOLD);
        milestoneText.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        milestoneText.setTextAlignment(TextAlignment.CENTER);
        milestoneText.setWrappingWidth(GameConfig.SCENE_WIDTH);
        milestoneText.setX(0);
        milestoneText.setY(210);

        Button backButton = GameUiFactory.createSmallButton("BACK");
        backButton.setLayoutX(30);
        backButton.setLayoutY(25);
        backButton.setOnAction(e -> {
            timer.stop();
            stopMusic();
            controller.showJourneySelect(playableStage.getJourneyId());
        });

        root.getChildren().addAll(
                stageText,
                progressBack,
                progressFill,
                progressText,
                scoreText,
                comboText,
                accuracyText,
                debugText,
                judgmentText,
                milestoneText,
                backButton
        );
    }

    private void createLaneGuides() {
        double playfieldWidth = PLAYFIELD_RIGHT - PLAYFIELD_LEFT;

        Rectangle hitLineGlow = new Rectangle(PLAYFIELD_LEFT, HIT_Y - 7, playfieldWidth, 14);
        hitLineGlow.setFill(Color.rgb(255, 255, 255, 0.08));
        hitLineGlow.setArcWidth(16);
        hitLineGlow.setArcHeight(16);

        Line hitLine = new Line(PLAYFIELD_LEFT, HIT_Y, PLAYFIELD_RIGHT, HIT_Y);
        hitLine.setStroke(theme.getAccentColor());
        hitLine.setStrokeWidth(3);
        hitLine.setOpacity(0.9);

        root.getChildren().addAll(hitLineGlow, hitLine);

        for (int lane = 0; lane < 4; lane++) {
            double x = LANE_X[lane] + 52;
            Line guide = new Line(x, 105, x, HIT_Y + 50);
            guide.setStroke(theme.laneColor(lane));
            guide.setStrokeWidth(2);
            guide.setOpacity(0.25);
            root.getChildren().add(guide);
        }
    }

    private void createReceptors() {
        receptors = new Group[4];

        for (int lane = 0; lane < 4; lane++) {
            Group receptor = NoteRenderer.createReceptor(LANE_X[lane] + 52, 525, lane, theme);
            receptors[lane] = receptor;

            root.getChildren().add(receptor);
        }
    }

    private AnimationTimer createTimer(List<Note> noteData) {
        double noteSpeed = GameConfig.BASE_NOTE_SPEED * options.getNoteSpeedMultiplier() * stageSpeedMultiplier();

        return new AnimationTimer() {
            private boolean finished;

            @Override
            public void handle(long now) {
                double rawTime = rawSecondsSinceStart();
                updateCountdown(rawTime);

                if (rawTime < COUNTDOWN_SECONDS) {
                    return;
                }

                countdownComplete = true;
                startMusicIfNeeded();
                double audioTime = gameplaySeconds();
                double currentTime = chartSeconds(audioTime);

                updateProgress(currentTime);
                updateBeatPulse(audioTime);
                updateDebugOverlay(audioTime, currentTime);
                spawnNotes(noteData, currentTime, noteSpeed);
                updateActiveNotes(currentTime, noteSpeed);

                if (!finished && currentTime >= chartEndTime) {
                    finished = true;
                    stop();
                    finishStage();
                }
            }
        };
    }

    private void createOverlays() {
        countdownText = new Text("3");
        countdownText.setFill(Color.GOLDENROD);
        countdownText.setFont(Font.font("Georgia", FontWeight.BOLD, 82));
        countdownText.setTextAlignment(TextAlignment.CENTER);
        countdownText.setWrappingWidth(GameConfig.SCENE_WIDTH);
        countdownText.setX(0);
        countdownText.setY(310);

        pauseOverlay = new VBox(22);
        pauseOverlay.setAlignment(javafx.geometry.Pos.CENTER);
        pauseOverlay.setPrefSize(GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
        pauseOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.82);");
        pauseOverlay.setVisible(false);

        Text pausedTitle = new Text("PAUSED");
        pausedTitle.setFill(Color.GOLDENROD);
        pausedTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 48));

        pauseStatsText = new Text("");
        pauseStatsText.setFill(Color.LIGHTGRAY);
        pauseStatsText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        pauseStatsText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button resumeButton = GameUiFactory.createSmallButton("RESUME");
        Button retryButton = GameUiFactory.createSmallButton("RETRY");
        Button playButton = GameUiFactory.createSmallButton("STAGES");
        Button optionsButton = GameUiFactory.createSmallButton("OPTIONS");

        resumeButton.setOnAction(e -> resumeGame());
        retryButton.setOnAction(e -> {
            timer.stop();
            stopMusic();
            controller.startStage(playableStage);
        });
        playButton.setOnAction(e -> {
            timer.stop();
            stopMusic();
            controller.showJourneySelect(playableStage.getJourneyId());
        });
        optionsButton.setOnAction(e -> {
            timer.stop();
            stopMusic();
            controller.showOptions();
        });

        pauseOverlay.getChildren().addAll(pausedTitle, pauseStatsText, resumeButton, retryButton, playButton, optionsButton);
        root.getChildren().addAll(countdownText, pauseOverlay);
    }

    private void updateCountdown(double rawTime) {
        if (rawTime < 1.0) {
            countdownText.setText("3");
            countdownText.setVisible(true);
        } else if (rawTime < 2.0) {
            countdownText.setText("2");
            countdownText.setVisible(true);
        } else if (rawTime < 3.0) {
            countdownText.setText("1");
            countdownText.setVisible(true);
        } else if (rawTime < 3.55) {
            countdownText.setText("START");
            countdownText.setVisible(true);
        } else {
            countdownText.setVisible(false);
        }
    }

    private void spawnNotes(List<Note> noteData, double currentTime, double noteSpeed) {
        for (Note note : noteData) {
            if (!note.isSpawned() && currentTime >= note.getTime() - GameConfig.SPAWN_LEAD_TIME) {
                Group noteGroup = NoteRenderer.create(LANE_X[note.getLane()], note, noteSpeed, theme);
                activeNotes.add(noteGroup);
                root.getChildren().add(noteGroup);
                note.setSpawned(true);
            }
        }
    }

    private void updateActiveNotes(double currentTime, double noteSpeed) {
        Iterator<Group> iterator = activeNotes.iterator();

        while (iterator.hasNext()) {
            Group noteGroup = iterator.next();
            Note note = (Note) noteGroup.getUserData();

            double y = HIT_Y - ((note.getTime() - currentTime) * noteSpeed);
            noteGroup.setLayoutY(y);

            if (note.isHolding()) {
                updateHeldNote(noteGroup, note, currentTime, iterator);
            }

            if (!note.isHit() && !note.isHolding() && currentTime > note.getTime() + hitJudge.badWindow()) {
                recordAndRemove(noteGroup, iterator, ScoreTracker.Judgment.MISS, "MISS", false, null);
            }
        }
    }

    private void updateHeldNote(Group noteGroup, Note note, double currentTime, Iterator<Group> iterator) {
        double progress = Math.max(0, Math.min(currentTime, note.getHoldEndTime()) - note.getTime());
        note.setHoldProgress(progress);

        if (keyHeld[note.getLane()]) {
            awardHoldTicks(note, currentTime);
        }

        double graceRatio = holdGraceRatio(note, currentTime);
        NoteRenderer.updateHoldVisuals(noteGroup, note, graceRatio, keyHeld[note.getLane()]);

        if (note.isReleaseGraceActive() && currentTime - note.getReleasedAt() > HOLD_RELEASE_GRACE) {
            note.setHoldDropped(true);
            ScoreTracker.Judgment judgment = finishHold(
                    note,
                    note.getReleasedAt(),
                    note.getReleasedAt() + options.getInputOffsetSeconds(),
                    false
            );
            recordAndRemove(noteGroup, iterator, judgment, "DROPPED", true, null);
            return;
        }

        if (keyHeld[note.getLane()] && currentTime >= note.getHoldEndTime()) {
            ScoreTracker.Judgment judgment = finishHold(note, currentTime, currentTime, true);
            recordAndRemove(
                    noteGroup,
                    iterator,
                    judgment,
                    JudgmentStyle.text(judgment),
                    true,
                    note.getHoldStartOffsetSeconds()
            );
        }
    }

    private void awardHoldTicks(Note note, double currentTime) {
        while (note.getNextHoldTickTime() <= currentTime && note.getNextHoldTickTime() < note.getHoldEndTime()) {
            scoreTracker.recordHoldTick();
            note.setNextHoldTickTime(note.getNextHoldTickTime() + HOLD_TICK_INTERVAL);
            updateScoreText();
        }
    }

    private double holdGraceRatio(Note note, double currentTime) {
        if (!note.isReleaseGraceActive()) {
            return 1.0;
        }

        return 1.0 - ((currentTime - note.getReleasedAt()) / HOLD_RELEASE_GRACE);
    }

    private void handleKeyPressed(KeyCode code) {
        if (handleCalibrationKey(code)) {
            return;
        }

        if (code == KeyCode.ESCAPE) {
            togglePause();
            return;
        }

        if (paused || !countdownComplete) {
            return;
        }

        int lane = laneForKey(code);

        if (lane == -1 || keyHeld[lane]) {
            return;
        }

        keyHeld[lane] = true;
        GameplayFeedback.setReceptorPressed(receptors[lane], true, theme.laneColor(lane));

        if (resumeGraceHold(lane)) {
            return;
        }

        checkArrowHit(lane, chartSeconds());
    }

    private void handleKeyReleased(KeyCode code) {
        if (paused || !countdownComplete) {
            return;
        }

        int lane = laneForKey(code);

        if (lane == -1) {
            return;
        }

        keyHeld[lane] = false;
        GameplayFeedback.setReceptorPressed(receptors[lane], false, theme.laneColor(lane));
        releaseHeldNote(lane, chartSeconds());
    }

    private void checkArrowHit(int lane, double currentTime) {
        double adjustedTime = currentTime + options.getInputOffsetSeconds();
        Group closestNote = null;
        double closestDelta = Double.MAX_VALUE;
        double closestSignedDelta = 0;

        for (Group noteGroup : activeNotes) {
            Note note = (Note) noteGroup.getUserData();

            if (note.getLane() == lane && !note.isHit() && !note.isHolding()) {
                double signedDelta = adjustedTime - note.getTime();
                double delta = Math.abs(signedDelta);

                if (delta < closestDelta) {
                    closestDelta = delta;
                    closestSignedDelta = signedDelta;
                    closestNote = noteGroup;
                }
            }
        }

        if (closestNote == null || !hitJudge.canHit(closestDelta)) {
            return;
        }

        Note note = (Note) closestNote.getUserData();
        ScoreTracker.Judgment judgment = hitJudge.judge(closestDelta);

        if (note.isHoldNote()) {
            note.setHolding(true);
            note.setHoldStartedAt(currentTime);
            note.setHoldStartOffsetSeconds(closestSignedDelta);
            note.setReleasedAt(0);
            note.setReleaseGraceActive(false);
            note.setHoldDropped(false);
            note.setNextHoldTickTime(Math.max(note.getTime(), currentTime) + HOLD_TICK_INTERVAL);
            note.setHoldStartJudgment(judgment);
            GameplayFeedback.showJudgment(
                    judgmentText,
                    JudgmentStyle.text(judgment),
                    JudgmentStyle.color(judgment)
            );
            GameplayFeedback.createHitBurst(root, closestNote, judgment);
            return;
        }

        note.setHit(true);
        scoreTracker.record(judgment);
        scoreTracker.recordTimingOffset(closestSignedDelta);
        recordNoteBonus(note, judgment, closestNote);
        showComboMilestoneIfNeeded();
        showJudgment(judgment);
        GameplayFeedback.createHitBurst(root, closestNote, judgment);
        showMissFeedbackIfNeeded(judgment);
        updateScoreText();
        GameplayFeedback.pulseCombo(comboText, scoreTracker.getCombo());
        root.getChildren().remove(closestNote);
        activeNotes.remove(closestNote);
    }

    private void releaseHeldNote(int lane, double currentTime) {
        double adjustedTime = currentTime + options.getInputOffsetSeconds();
        Iterator<Group> iterator = activeNotes.iterator();

        while (iterator.hasNext()) {
            Group noteGroup = iterator.next();
            Note note = (Note) noteGroup.getUserData();

            if (note.getLane() == lane && note.isHolding()) {
                double releaseDelta = Math.abs(adjustedTime - note.getHoldEndTime());
                double signedReleaseDelta = adjustedTime - note.getHoldEndTime();

                if (hitJudge.canHit(releaseDelta)) {
                    ScoreTracker.Judgment judgment = finishHold(note, currentTime, adjustedTime, false);
                    recordAndRemove(
                            noteGroup,
                            iterator,
                            judgment,
                            JudgmentStyle.text(judgment),
                            true,
                            holdTimingOffset(note, signedReleaseDelta)
                    );
                    return;
                }

                note.setReleasedAt(currentTime);
                note.setReleaseGraceActive(true);
                GameplayFeedback.showJudgment(judgmentText, "HOLD GRACE", Color.ORANGE);
                NoteRenderer.updateHoldVisuals(noteGroup, note, 1.0, false);
                return;
            }
        }
    }

    private boolean resumeGraceHold(int lane) {
        for (Group noteGroup : activeNotes) {
            Note note = (Note) noteGroup.getUserData();

            if (note.getLane() == lane && note.isHolding() && note.isReleaseGraceActive()) {
                note.setReleaseGraceActive(false);
                GameplayFeedback.showJudgment(judgmentText, "HOLDING", Color.LIGHTGREEN);
                NoteRenderer.updateHoldVisuals(noteGroup, note, 1.0, true);
                return true;
            }
        }

        return false;
    }

    private ScoreTracker.Judgment finishHold(
            Note note,
            double releaseTime,
            double adjustedReleaseTime,
            boolean heldThroughEnd
    ) {
        note.setHolding(false);
        note.setHit(true);
        note.setReleaseGraceActive(false);

        ScoreTracker.Judgment startJudgment = note.getHoldStartJudgment();
        if (startJudgment == null) {
            startJudgment = ScoreTracker.Judgment.BAD;
        }

        if (heldThroughEnd) {
            return startJudgment;
        }

        double heldFrom = Math.max(note.getTime(), note.getHoldStartedAt());
        double heldRatio = Math.max(0, releaseTime - heldFrom) / note.getHoldDuration();
        double releaseDelta = Math.abs(adjustedReleaseTime - note.getHoldEndTime());

        if (note.isHoldDropped()) {
            if (heldRatio >= 0.75) {
                return ScoreTracker.Judgment.BAD;
            }

            return ScoreTracker.Judgment.MISS;
        }

        if (hitJudge.canHit(releaseDelta)) {
            return hitJudge.worse(startJudgment, hitJudge.judge(releaseDelta));
        }

        if (heldRatio >= 0.75) {
            return hitJudge.worse(startJudgment, ScoreTracker.Judgment.GOOD);
        }

        if (heldRatio >= 0.45) {
            return ScoreTracker.Judgment.BAD;
        }

        return ScoreTracker.Judgment.MISS;
    }

    private void recordAndRemove(
            Group noteGroup,
            Iterator<Group> iterator,
            ScoreTracker.Judgment judgment,
            String judgmentTextValue,
            boolean showBurst,
            Double signedTimingOffset
    ) {
        Note note = (Note) noteGroup.getUserData();
        note.setHit(true);
        scoreTracker.record(judgment);

        if (signedTimingOffset != null && judgment != ScoreTracker.Judgment.MISS) {
            scoreTracker.recordTimingOffset(signedTimingOffset);
        }

        recordNoteBonus(note, judgment, noteGroup);
        showComboMilestoneIfNeeded();

        if (note.isHoldNote() && judgment != ScoreTracker.Judgment.MISS) {
            scoreTracker.recordHoldReleaseBonus(judgment);
        }

        GameplayFeedback.showJudgment(judgmentText, judgmentTextValue, JudgmentStyle.color(judgment));

        if (showBurst) {
            GameplayFeedback.createHitBurst(root, noteGroup, judgment);
        }

        showMissFeedbackIfNeeded(judgment);
        updateScoreText();
        GameplayFeedback.pulseCombo(comboText, scoreTracker.getCombo());
        root.getChildren().remove(noteGroup);
        iterator.remove();
    }

    private void showJudgment(ScoreTracker.Judgment judgment) {
        GameplayFeedback.showJudgment(judgmentText, JudgmentStyle.text(judgment), JudgmentStyle.color(judgment));
    }

    private double holdTimingOffset(Note note, double signedReleaseDelta) {
        return (note.getHoldStartOffsetSeconds() + signedReleaseDelta) / 2.0;
    }

    private void updateScoreText() {
        scoreText.setText("Score " + scoreTracker.getScore());
        comboText.setText("Combo " + scoreTracker.getCombo());
        comboText.setFill(scoreTracker.getCombo() >= 25 ? Color.GOLD : Color.WHITE);
        accuracyText.setText(String.format("%.1f%%", scoreTracker.getCurrentAccuracy()));
    }

    private void updateProgress(double currentTime) {
        if (progressFill == null || progressText == null) {
            return;
        }

        double progress = chartEndTime <= 0 ? 0 : Math.max(0, Math.min(currentTime / chartEndTime, 1.0));
        progressFill.setWidth(340 * progress);
        progressText.setText(formatTime(currentTime) + " / " + formatTime(chartEndTime));
    }

    private void showComboMilestoneIfNeeded() {
        int combo = scoreTracker.getCombo();

        if (combo == 10 || combo == 25 || combo == 50 || (combo >= 100 && combo % 100 == 0)) {
            GameplayFeedback.showStageBanner(milestoneText, combo + " COMBO", Color.GOLD);
        }
    }

    private double getChartEndTime(List<Note> notes) {
        double endTime = 0;

        for (Note note : notes) {
            endTime = Math.max(endTime, note.getTime() + note.getHoldDuration() + hitJudge.badWindow());
        }

        return endTime;
    }

    private double gameplaySeconds() {
        if (musicStarted && musicPlayer.hasMusic()) {
            return Math.max(0, musicPlayer.currentTimeSeconds());
        }

        return Math.max(0, rawSecondsSinceStart() - COUNTDOWN_SECONDS);
    }

    private double chartSeconds() {
        return chartSeconds(gameplaySeconds());
    }

    private double chartSeconds(double audioTime) {
        return Math.max(0, audioTime - stageChartOffsetSeconds());
    }

    private double stageChartOffsetSeconds() {
        return options.getStageChartOffsetSeconds(playableStage);
    }

    private double rawSecondsSinceStart() {
        return (System.nanoTime() - startTime) / 1_000_000_000.0;
    }

    private boolean handleCalibrationKey(KeyCode code) {
        if (code == KeyCode.OPEN_BRACKET) {
            options.adjustStageChartOffsetMillis(playableStage, -10);
            showCalibrationOffset();
            return true;
        }

        if (code == KeyCode.CLOSE_BRACKET) {
            options.adjustStageChartOffsetMillis(playableStage, 10);
            showCalibrationOffset();
            return true;
        }

        if (code == KeyCode.DIGIT0) {
            options.resetStageChartOffset(playableStage);
            showCalibrationOffset();
            return true;
        }

        return false;
    }

    private void showCalibrationOffset() {
        GameplayFeedback.showStageBanner(
                milestoneText,
                String.format("CHART OFFSET %+dms", Math.round(stageChartOffsetSeconds() * 1000)),
                theme.getSoftTextColor()
        );
    }

    private void togglePause() {
        if (paused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    private void pauseGame() {
        if (paused) {
            return;
        }

        paused = true;
        pauseStartedAt = System.nanoTime();
        timer.stop();
        musicPlayer.pause();

        for (int lane = 0; lane < keyHeld.length; lane++) {
            keyHeld[lane] = false;
            GameplayFeedback.setReceptorPressed(receptors[lane], false, theme.laneColor(lane));
        }

        updatePauseStats();
        pauseOverlay.setVisible(true);
    }

    private void resumeGame() {
        if (!paused) {
            return;
        }

        startTime += System.nanoTime() - pauseStartedAt;
        paused = false;
        pauseOverlay.setVisible(false);
        if (musicStarted) {
            musicPlayer.resume();
        }
        timer.start();
    }

    private void updatePauseStats() {
        if (pauseStatsText == null) {
            return;
        }

        pauseStatsText.setText(
                "Score "
                        + scoreTracker.getScore()
                        + "   Combo "
                        + scoreTracker.getCombo()
                        + "   Accuracy "
                        + String.format("%.1f%%", scoreTracker.getCurrentAccuracy())
                        + "\nTime "
                        + formatTime(chartSeconds())
                        + " / "
                        + formatTime(chartEndTime)
                        + "   Chart Offset "
                        + String.format("%+dms", Math.round(stageChartOffsetSeconds() * 1000))
        );
    }

    private int laneForKey(KeyCode code) {
        return switch (code) {
            case LEFT, A -> 0;
            case UP, U -> 1;
            case DOWN, N -> 2;
            case RIGHT, D -> 3;
            default -> -1;
        };
    }

    private double stageSpeedMultiplier() {
        return 1.0 + (playableStage.getIndex() * 0.04);
    }

    private String formatTime(double seconds) {
        int totalSeconds = Math.max(0, (int) Math.floor(seconds));
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private void finishStage() {
        stopMusic();

        if (isFullCombo()) {
            GameplayFeedback.showStageBanner(milestoneText, "FULL COMBO", Color.GOLD);

            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(e -> controller.showStageResultStory(playableStage, scoreTracker));
            delay.play();
            return;
        }

        controller.showStageResultStory(playableStage, scoreTracker);
    }

    private boolean isFullCombo() {
        return scoreTracker.getTotalJudged() == scoreTracker.getTotalNotes()
                && scoreTracker.getBadCount() == 0
                && scoreTracker.getMissCount() == 0;
    }

    private void showMissFeedbackIfNeeded(ScoreTracker.Judgment judgment) {
        if (judgment == ScoreTracker.Judgment.BAD || judgment == ScoreTracker.Judgment.MISS) {
            GameplayFeedback.shake(root);
        }
    }

    private void updateBeatPulse(double currentTime) {
        if (beatProfile == null || currentTime < beatProfile.firstBeatOffset()) {
            return;
        }

        int beatIndex = (int) Math.floor((currentTime - beatProfile.firstBeatOffset()) / beatProfile.beatSeconds());

        if (beatIndex == lastBeatIndex) {
            return;
        }

        lastBeatIndex = beatIndex;
        GameplayFeedback.pulseBeat(root, receptors, theme, beatIndex % beatProfile.beatsPerMeasure() == 0);
    }

    private void updateDebugOverlay(double audioTime, double currentTime) {
        if (debugText == null) {
            return;
        }

        if (beatProfile == null) {
            debugText.setText(String.format("Audio %.3f | Chart %.3f", audioTime, currentTime));
            return;
        }

        if (currentTime < beatProfile.firstBeatOffset()) {
            debugText.setText(String.format(
                    "Audio %.3f | Chart %.3f | Offset %+dms | waiting for first beat",
                    audioTime,
                    currentTime,
                    Math.round(stageChartOffsetSeconds() * 1000)
            ));
            return;
        }

        int slotsPerMeasure = beatProfile.beatsPerMeasure() * beatProfile.gridSlotsPerBeat();
        int totalSlot = (int) Math.floor((currentTime - beatProfile.firstBeatOffset()) / beatProfile.gridSlotSeconds());
        int measure = totalSlot / slotsPerMeasure;
        int slot = totalSlot % slotsPerMeasure;
        int beat = (slot / beatProfile.gridSlotsPerBeat()) + 1;
        int beatSlot = slot % beatProfile.gridSlotsPerBeat();

        debugText.setText(String.format(
                "Audio %.3f | Chart %.3f | M %02d B %d Slot %02d (%d/%d) | Offset %+dms",
                audioTime,
                currentTime,
                measure + 1,
                beat,
                slot,
                beatSlot,
                beatProfile.gridSlotsPerBeat(),
                Math.round(stageChartOffsetSeconds() * 1000)
        ));
    }

    private void recordNoteBonus(Note note, ScoreTracker.Judgment judgment, Group noteGroup) {
        if (!note.isGoldNote()) {
            return;
        }

        scoreTracker.recordGoldNoteBonus(judgment);

        if (judgment != ScoreTracker.Judgment.MISS) {
            GameplayFeedback.createGoldBurst(root, noteGroup, theme);
            GameplayFeedback.showStageBanner(milestoneText, "GOLD NOTE", theme.getGoldNoteColor());
        }
    }

    private void startMusicIfNeeded() {
        if (musicStarted) {
            return;
        }

        musicStarted = true;
        musicPlayer.playFromStart();
    }

    private void stopMusic() {
        musicPlayer.stop();
        musicPlayer.dispose();
    }
}
