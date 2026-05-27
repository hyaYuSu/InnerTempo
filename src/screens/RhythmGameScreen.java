package screens;

import audio.StageMusicPlayer;
import config.AssetCatalog;
import config.GameConfig;
import gameplay.GameplayEffects;
import gameplay.HitJudge;
import gameplay.RhythmInputHandler;
import gameplay.StageBeatProfile;
import gameplay.StageChartGenerator;
import manager.ScreenManager;
import model.Note;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import settings.GameplaySettings;
import ui.AnimatedGifBackground;
import ui.GameplayFeedback;
import ui.JudgmentStyle;
import ui.JourneyTheme;
import ui.NoteRenderer;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class RhythmGameScreen {
    private static final double COUNTDOWN_SECONDS = 3.0;
    private static final double HOLD_TICK_INTERVAL = 0.25;
    private static final double HOLD_RELEASE_GRACE = 0.12;
    private static final double HIT_Y = 500;
    private static final double PLAYFIELD_LEFT = 220;
    private static final double PLAYFIELD_RIGHT = 780;
    private static final double[] LANE_X = {238, 378, 518, 658};
    private static final long COMBO_PULSE_NANOS = 140_000_000L;
    private static final long SHAKE_NANOS = 160_000_000L;
    private static final int END_FADE_MILLIS = 650;
    private static final int FINISH_HOLD_MILLIS = 650;
    private static final int RESUME_COUNTDOWN_MILLIS = 2400;

    private final ScreenManager controller;
    private final GameplaySettings options;
    private final StageChartGenerator chartGenerator;
    private final PlayableStage playableStage;
    private final HitJudge hitJudge;
    private final RhythmInputHandler inputHandler;
    private final JourneyTheme theme;
    private final StageBeatProfile beatProfile;
    private final AnimatedGifBackground backgroundImage;
    private final GameplayEffects effects = new GameplayEffects();

    private final List<NoteVisual> activeNotes = new ArrayList<>();
    private final boolean[] keyHeld = new boolean[4];

    private GamePanel root;
    private PauseMenu pauseMenu;
    private List<Note> noteData;
    private ScoreTracker scoreTracker;
    private StageMusicPlayer musicPlayer;
    private Timer timer;
    private Timer transitionTimer;
    private Timer backgroundAnimationTimer;
    private Timer resumeCountdownTimer;
    private long startTime;
    private long pauseStartedAt;
    private long judgmentStartedAt;
    private long milestoneStartedAt;
    private long comboPulseStartedAt;
    private long shakeStartedAt;
    private long endFadeStartedAt;
    private long finishStartedAt;
    private long resumeCountdownStartedAt;
    private boolean paused;
    private boolean countdownComplete;
    private boolean musicStarted;
    private boolean finished;
    private boolean endingTransition;
    private boolean resumeCountdownActive;
    private int lastBeatIndex = -1;
    private double chartEndTime;
    private double noteSpeed;
    private double currentChartTime;
    private String countdownText = "3";
    private boolean countdownVisible = true;
    private String judgmentText = "";
    private Color judgmentColor = Color.WHITE;
    private String milestoneText = "";
    private Color milestoneColor = Color.WHITE;
    private boolean musicDisposed;

    public RhythmGameScreen(
            ScreenManager controller,
            GameplaySettings options,
            StageChartGenerator chartGenerator,
            PlayableStage playableStage
    ) {
        this.controller = controller;
        this.options = options;
        this.chartGenerator = chartGenerator;
        this.playableStage = playableStage;
        this.hitJudge = new HitJudge(options);
        this.inputHandler = new RhythmInputHandler(options);
        this.theme = JourneyTheme.forStage(playableStage);
        this.beatProfile = StageBeatProfile.forStage(playableStage).orElse(null);
        this.backgroundImage = loadBackgroundImage(playableStage);
    }

    public JPanel create() {
        root = new GamePanel();
        root.setLayout(null);
        root.setFocusable(true);

        musicPlayer = new StageMusicPlayer(playableStage, options.getMusicVolume());
        noteData = chartGenerator.generateChart(playableStage, musicPlayer.durationSeconds());
        scoreTracker = new ScoreTracker(noteData.size());
        chartEndTime = getChartEndTime(noteData) + 0.6;
        noteSpeed = GameConfig.BASE_NOTE_SPEED * options.getNoteSpeedMultiplier() * stageSpeedMultiplier();
        currentChartTime = 0;

        createPauseMenu();
        installInputBindings();

        startTime = System.nanoTime();
        timer = new Timer(16, e -> tick());
        timer.start();
        startBackgroundAnimationTimer();

        return root;
    }

    private void createPauseMenu() {
        pauseMenu = new PauseMenu(
                this::togglePause,
                this::resumeGame,
                () -> leaveGameplay(() -> controller.startStage(playableStage)),
                () -> leaveGameplay(() -> controller.showJourneySelect(playableStage.getJourneyId())),
                this::openOptionsFromPause
        );
        pauseMenu.install(root);
    }

    private void openOptionsFromPause() {
        controller.showOptions(() -> {
            musicPlayer.setVolume(options.getMusicVolume());
            installInputBindings();
            controller.restoreScreen(root);
            root.requestFocusInWindow();
        }, backgroundImage);
    }

    private void installInputBindings() {
        inputHandler.install(root, this::handleKeyPressed, this::handleKeyReleased, this::togglePause);
    }

    private void tick() {
        effects.prune();

        double rawTime = rawSecondsSinceStart();
        updateCountdown(rawTime);

        if (rawTime < COUNTDOWN_SECONDS) {
            double previewChartTime = rawTime - COUNTDOWN_SECONDS;
            currentChartTime = previewChartTime;
            spawnNotes(noteData, previewChartTime, noteSpeed);
            updateNotePositions(previewChartTime, noteSpeed);
            root.repaint();
            return;
        }

        countdownComplete = true;
        startMusicIfNeeded();
        double audioTime = gameplaySeconds();
        double currentTime = audioTime;
        currentChartTime = currentTime;

        updateBeatPulse(audioTime);
        spawnNotes(noteData, currentTime, noteSpeed);
        updateActiveNotes(currentTime, noteSpeed);

        if (!finished && shouldFinishStage(currentTime)) {
            finished = true;
            timer.stop();
            finishStage();
        }

        root.repaint();
    }

    private void updateCountdown(double rawTime) {
        if (rawTime < 1.0) {
            countdownText = "3";
            countdownVisible = true;
        } else if (rawTime < 2.0) {
            countdownText = "2";
            countdownVisible = true;
        } else if (rawTime < 3.0) {
            countdownText = "1";
            countdownVisible = true;
        } else if (rawTime < 3.55) {
            countdownText = "START";
            countdownVisible = true;
        } else {
            countdownVisible = false;
        }
    }

    private void spawnNotes(List<Note> notes, double currentTime, double pixelsPerSecond) {
        for (Note note : notes) {
            if (!note.isSpawned() && currentTime >= note.getTime() - GameConfig.SPAWN_LEAD_TIME) {
                activeNotes.add(new NoteVisual(note, LANE_X[note.getLane()], HIT_Y - ((note.getTime() - currentTime) * pixelsPerSecond)));
                note.setSpawned(true);
            }
        }
    }

    private void updateActiveNotes(double currentTime, double pixelsPerSecond) {
        Iterator<NoteVisual> iterator = activeNotes.iterator();

        while (iterator.hasNext()) {
            NoteVisual noteVisual = iterator.next();
            Note note = noteVisual.note();

            updateNotePosition(noteVisual, currentTime, pixelsPerSecond);

            if (note.isHolding() && updateHeldNote(noteVisual, note, currentTime, iterator)) {
                continue;
            }

            if (!note.isHit() && !note.isHolding() && currentTime > note.getTime() + hitJudge.badWindow()) {
                recordAndRemove(noteVisual, iterator, ScoreTracker.Judgment.MISS, "MISS", false, null);
            }
        }
    }

    private void updateNotePositions(double currentTime, double pixelsPerSecond) {
        for (NoteVisual noteVisual : activeNotes) {
            updateNotePosition(noteVisual, currentTime, pixelsPerSecond);
        }
    }

    private void updateNotePosition(NoteVisual noteVisual, double currentTime, double pixelsPerSecond) {
        Note note = noteVisual.note();
        noteVisual.setY(HIT_Y - ((note.getTime() - currentTime) * pixelsPerSecond));
    }

    private boolean updateHeldNote(
            NoteVisual noteVisual,
            Note note,
            double currentTime,
            Iterator<NoteVisual> iterator
    ) {
        double progress = Math.max(0, Math.min(currentTime, note.getHoldEndTime()) - note.getTime());
        note.setHoldProgress(progress);

        if (keyHeld[note.getLane()]) {
            awardHoldTicks(note, currentTime);
        }

        if (note.isReleaseGraceActive() && currentTime - note.getReleasedAt() > HOLD_RELEASE_GRACE) {
            note.setHoldDropped(true);
            ScoreTracker.Judgment judgment = finishHold(
                    note,
                    note.getReleasedAt(),
                    note.getReleasedAt(),
                    false
            );
            recordAndRemove(noteVisual, iterator, judgment, "DROPPED", true, null);
            return true;
        }

        if (keyHeld[note.getLane()] && currentTime >= note.getHoldEndTime()) {
            ScoreTracker.Judgment judgment = finishHold(note, currentTime, currentTime, true);
            recordAndRemove(
                    noteVisual,
                    iterator,
                    judgment,
                    JudgmentStyle.text(judgment),
                    true,
                    note.getHoldStartOffsetSeconds()
            );
            return true;
        }

        return false;
    }

    private void awardHoldTicks(Note note, double currentTime) {
        while (note.getNextHoldTickTime() <= currentTime && note.getNextHoldTickTime() < note.getHoldEndTime()) {
            scoreTracker.recordHoldTick();
            note.setNextHoldTickTime(note.getNextHoldTickTime() + HOLD_TICK_INTERVAL);
        }
    }

    private void handleKeyPressed(int code) {
        if (paused || !countdownComplete) {
            return;
        }

        int lane = laneForKey(code);

        if (lane == -1 || keyHeld[lane]) {
            return;
        }

        keyHeld[lane] = true;

        if (resumeGraceHold(lane)) {
            return;
        }

        checkArrowHit(lane, gameplaySeconds());
        root.repaint();
    }

    private void handleKeyReleased(int code) {
        if (paused || !countdownComplete) {
            return;
        }

        int lane = laneForKey(code);

        if (lane == -1) {
            return;
        }

        keyHeld[lane] = false;
        releaseHeldNote(lane, gameplaySeconds());
        root.repaint();
    }

    private void checkArrowHit(int lane, double currentTime) {
        double adjustedTime = currentTime;
        NoteVisual closestNote = null;
        double closestDelta = Double.MAX_VALUE;
        double closestSignedDelta = 0;

        for (NoteVisual noteVisual : activeNotes) {
            Note note = noteVisual.note();

            if (note.getLane() == lane && !note.isHit() && !note.isHolding()) {
                double signedDelta = adjustedTime - note.getTime();
                double delta = Math.abs(signedDelta);

                if (delta < closestDelta) {
                    closestDelta = delta;
                    closestSignedDelta = signedDelta;
                    closestNote = noteVisual;
                }
            }
        }

        if (closestNote == null || !hitJudge.canHit(closestDelta)) {
            return;
        }

        Note note = closestNote.note();
        ScoreTracker.Judgment judgment = hitJudge.judge(closestDelta);

        if (note.isHoldNote()) {
            if (closestSignedDelta > hitJudge.goodWindow()) {
                return;
            }

            note.setHolding(true);
            note.setHoldStartedAt(currentTime);
            note.setHoldStartOffsetSeconds(closestSignedDelta);
            note.setReleasedAt(0);
            note.setReleaseGraceActive(false);
            note.setHoldDropped(false);
            note.setNextHoldTickTime(Math.max(note.getTime(), currentTime) + HOLD_TICK_INTERVAL);
            note.setHoldStartJudgment(judgment);
            showJudgment(JudgmentStyle.text(judgment), JudgmentStyle.color(judgment));
            createHitBurst(closestNote, judgment);
            return;
        }

        note.setHit(true);
        scoreTracker.record(judgment);
        scoreTracker.recordTimingOffset(closestSignedDelta);
        recordNoteBonus(note, judgment, closestNote);
        showComboMilestoneIfNeeded();
        showJudgment(judgment);
        createHitBurst(closestNote, judgment);
        showMissFeedbackIfNeeded(judgment);
        pulseCombo(scoreTracker.getCombo());
        activeNotes.remove(closestNote);
    }

    private void releaseHeldNote(int lane, double currentTime) {
        double adjustedTime = currentTime;
        Iterator<NoteVisual> iterator = activeNotes.iterator();

        while (iterator.hasNext()) {
            NoteVisual noteVisual = iterator.next();
            Note note = noteVisual.note();

            if (note.getLane() == lane && note.isHolding()) {
                double releaseDelta = Math.abs(adjustedTime - note.getHoldEndTime());
                double signedReleaseDelta = adjustedTime - note.getHoldEndTime();

                if (hitJudge.canHit(releaseDelta)) {
                    ScoreTracker.Judgment judgment = finishHold(note, currentTime, adjustedTime, false);
                    recordAndRemove(
                            noteVisual,
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
                showJudgment("HOLD GRACE", Color.ORANGE);
                return;
            }
        }
    }

    private boolean resumeGraceHold(int lane) {
        for (NoteVisual noteVisual : activeNotes) {
            Note note = noteVisual.note();

            if (note.getLane() == lane && note.isHolding() && note.isReleaseGraceActive()) {
                note.setReleaseGraceActive(false);
                showJudgment("HOLDING", new Color(144, 238, 144));
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
            NoteVisual noteVisual,
            Iterator<NoteVisual> iterator,
            ScoreTracker.Judgment judgment,
            String judgmentTextValue,
            boolean showBurst,
            Double signedTimingOffset
    ) {
        Note note = noteVisual.note();
        note.setHit(true);
        scoreTracker.record(judgment);

        if (signedTimingOffset != null && judgment != ScoreTracker.Judgment.MISS) {
            scoreTracker.recordTimingOffset(signedTimingOffset);
        }

        recordNoteBonus(note, judgment, noteVisual);
        showComboMilestoneIfNeeded();

        if (note.isHoldNote() && judgment != ScoreTracker.Judgment.MISS) {
            scoreTracker.recordHoldReleaseBonus(judgment);
        }

        showJudgment(judgmentTextValue, JudgmentStyle.color(judgment));

        if (showBurst) {
            createHitBurst(noteVisual, judgment);
        }

        showMissFeedbackIfNeeded(judgment);
        pulseCombo(scoreTracker.getCombo());
        iterator.remove();
    }

    private void showJudgment(ScoreTracker.Judgment judgment) {
        showJudgment(JudgmentStyle.text(judgment), JudgmentStyle.color(judgment));
    }

    private void showJudgment(String text, Color color) {
        judgmentText = text;
        judgmentColor = color;
        judgmentStartedAt = System.nanoTime();
    }

    private double holdTimingOffset(Note note, double signedReleaseDelta) {
        return (note.getHoldStartOffsetSeconds() + signedReleaseDelta) / 2.0;
    }

    private void showComboMilestoneIfNeeded() {
        int combo = scoreTracker.getCombo();

        if (combo == 10 || combo == 25 || combo == 50 || (combo >= 100 && combo % 100 == 0)) {
            showStageBanner(combo + " COMBO", new Color(255, 215, 0));
        }
    }

    private double getChartEndTime(List<Note> notes) {
        double endTime = 0;

        for (Note note : notes) {
            endTime = Math.max(endTime, note.getTime() + note.getHoldDuration() + hitJudge.badWindow());
        }

        return endTime;
    }

    private boolean shouldFinishStage(double currentTime) {
        return currentTime >= chartEndTime || (musicStarted && musicPlayer.hasMusic() && musicPlayer.isFinished());
    }

    private double gameplaySeconds() {
        if (musicStarted && musicPlayer.hasMusic()) {
            return Math.max(0, musicPlayer.currentTimeSeconds());
        }

        return Math.max(0, rawSecondsSinceStart() - COUNTDOWN_SECONDS);
    }

    private double rawSecondsSinceStart() {
        return (System.nanoTime() - startTime) / 1_000_000_000.0;
    }

    private void togglePause() {
        if (resumeCountdownActive) {
            return;
        }

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
        }

        setPauseControlsVisible(true);
        root.repaint();
    }

    private void resumeGame() {
        if (!paused || resumeCountdownActive) {
            return;
        }

        startResumeCountdown();
    }

    private void startResumeCountdown() {
        resumeCountdownActive = true;
        resumeCountdownStartedAt = System.nanoTime();
        setPauseControlsVisible(false);

        resumeCountdownTimer = new Timer(16, e -> {
            if (resumeCountdownProgress() >= 1.0) {
                finishResumeCountdown();
                return;
            }

            root.repaint();
        });
        resumeCountdownTimer.start();
        root.repaint();
    }

    private void finishResumeCountdown() {
        if (resumeCountdownTimer != null) {
            resumeCountdownTimer.stop();
            resumeCountdownTimer = null;
        }

        startTime += System.nanoTime() - pauseStartedAt;
        paused = false;
        resumeCountdownActive = false;
        setPauseControlsVisible(false);
        if (musicStarted) {
            musicPlayer.resume();
        }
        timer.start();
        root.repaint();
    }

    private double resumeCountdownProgress() {
        if (!resumeCountdownActive) {
            return 0;
        }

        double elapsedMillis = (System.nanoTime() - resumeCountdownStartedAt) / 1_000_000.0;
        return Math.max(0, Math.min(1, elapsedMillis / RESUME_COUNTDOWN_MILLIS));
    }

    private String resumeCountdownText() {
        double elapsedMillis = (System.nanoTime() - resumeCountdownStartedAt) / 1_000_000.0;
        int remaining = 3 - (int) Math.floor(elapsedMillis / 800.0);
        return Integer.toString(Math.max(1, remaining));
    }

    private void setPauseControlsVisible(boolean visible) {
        pauseMenu.setMenuVisible(visible);
    }

    private int laneForKey(int code) {
        return options.laneForKey(code);
    }

    private double stageSpeedMultiplier() {
        return 1.0 + (playableStage.getIndex() * 0.04);
    }

    private String formatTime(double seconds) {
        int totalSeconds = Math.max(0, (int) Math.floor(seconds));
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private AnimatedGifBackground loadBackgroundImage(PlayableStage stage) {
        return AnimatedGifBackground.load(AssetCatalog.gameplayBackgroundUrlFor(stage));
    }

    private void finishStage() {
        if (endingTransition) {
            return;
        }

        scoreRemainingNotesAsMisses();
        stopMusic();
        pauseMenu.hidePauseButton();
        endingTransition = true;
        finishStartedAt = System.nanoTime();
        endFadeStartedAt = 0;

        if (isFullCombo()) {
            showStageBanner("FULL COMBO", new Color(255, 215, 0));
        } else {
            showStageBanner("STAGE COMPLETE", theme.getAccentColor());
        }

        transitionTimer = new Timer(16, e -> {
            long now = System.nanoTime();
            if (endFadeStartedAt == 0
                    && (now - finishStartedAt) / 1_000_000.0 >= FINISH_HOLD_MILLIS) {
                endFadeStartedAt = now;
            }

            root.repaint();
            if (endFadeAlpha() >= 1.0) {
                transitionTimer.stop();
                stopBackgroundAnimationTimer();
                controller.showStageResultStory(playableStage, scoreTracker);
            }
        });
        transitionTimer.start();
        root.repaint();
    }

    private void scoreRemainingNotesAsMisses() {
        for (Note note : noteData) {
            if (!note.isHit()) {
                note.setHit(true);
                note.setHolding(false);
                note.setReleaseGraceActive(false);
                scoreTracker.record(ScoreTracker.Judgment.MISS);

                if (note.isGoldNote()) {
                    scoreTracker.recordGoldNoteBonus(ScoreTracker.Judgment.MISS);
                }
            }
        }

        activeNotes.clear();
        for (int lane = 0; lane < keyHeld.length; lane++) {
            keyHeld[lane] = false;
        }
    }

    private double endFadeAlpha() {
        if (!endingTransition || endFadeStartedAt == 0) {
            return 0;
        }

        double elapsedMillis = (System.nanoTime() - endFadeStartedAt) / 1_000_000.0;
        return Math.max(0, Math.min(1, elapsedMillis / END_FADE_MILLIS));
    }

    private boolean isFullCombo() {
        return scoreTracker.getTotalJudged() == scoreTracker.getTotalNotes()
                && scoreTracker.getBadCount() == 0
                && scoreTracker.getMissCount() == 0;
    }

    private void showMissFeedbackIfNeeded(ScoreTracker.Judgment judgment) {
        if (judgment == ScoreTracker.Judgment.BAD || judgment == ScoreTracker.Judgment.MISS) {
            shakeStartedAt = System.nanoTime();
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
        effects.addBeatPulse(beatIndex % beatProfile.beatsPerMeasure() == 0);
    }

    private void recordNoteBonus(Note note, ScoreTracker.Judgment judgment, NoteVisual noteVisual) {
        if (!note.isGoldNote()) {
            return;
        }

        scoreTracker.recordGoldNoteBonus(judgment);

        if (judgment != ScoreTracker.Judgment.MISS) {
            createGoldBurst(noteVisual);
            showStageBanner("GOLD NOTE", theme.getGoldNoteColor());
        }
    }

    private void startMusicIfNeeded() {
        if (musicStarted) {
            return;
        }

        musicStarted = true;
        musicPlayer.playFromStart();
    }

    private void leaveGameplay(Runnable navigation) {
        if (timer != null) {
            timer.stop();
        }
        if (transitionTimer != null) {
            transitionTimer.stop();
        }
        if (resumeCountdownTimer != null) {
            resumeCountdownTimer.stop();
            resumeCountdownTimer = null;
        }
        stopBackgroundAnimationTimer();
        stopMusic();
        navigation.run();
    }

    private void startBackgroundAnimationTimer() {
        if (backgroundImage == null || !backgroundImage.isAnimated()) {
            return;
        }

        backgroundAnimationTimer = new Timer(33, e -> {
            if (root != null && (paused || endingTransition || timer == null || !timer.isRunning())) {
                root.repaint();
            }
        });
        backgroundAnimationTimer.start();
    }

    private void stopBackgroundAnimationTimer() {
        if (backgroundAnimationTimer != null) {
            backgroundAnimationTimer.stop();
            backgroundAnimationTimer = null;
        }
    }

    private void stopMusic() {
        if (musicPlayer == null || musicDisposed) {
            return;
        }

        musicPlayer.stop();
        musicPlayer.dispose();
        musicDisposed = true;
    }

    private void createHitBurst(NoteVisual noteVisual, ScoreTracker.Judgment judgment) {
        effects.addHitBurst(
                noteVisual.centerX(),
                JudgmentStyle.color(judgment),
                judgment == ScoreTracker.Judgment.PERFECT,
                false
        );
    }

    private void createGoldBurst(NoteVisual noteVisual) {
        effects.addHitBurst(noteVisual.centerX(), theme.getGoldNoteColor(), true, true);
    }

    private void showStageBanner(String text, Color color) {
        milestoneText = text;
        milestoneColor = color;
        milestoneStartedAt = System.nanoTime();
    }

    private void pulseCombo(int combo) {
        if (combo > 1) {
            comboPulseStartedAt = System.nanoTime();
        }
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackground(g);

            int shakeOffset = currentShakeOffset();
            g.translate(shakeOffset, 0);
            drawLaneGuides(g);
            drawBeatPulses(g);
            drawActiveNotes(g);
            drawReceptors(g);
            drawHitBursts(g);
            drawHud(g);
            drawCountdown(g);
            drawPauseOverlay(g);
            drawEndFadeOverlay(g);
            g.dispose();
        }

        private void paintBackground(Graphics2D g) {
            g.setPaint(new GradientPaint(
                    0,
                    0,
                    theme.getBackgroundTop(),
                    0,
                    getHeight(),
                    theme.getBackgroundBottom()
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                drawCoverImage(g, frame);
                g.setColor(new Color(1, 7, 18, 132));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 35), 0, getHeight(), new Color(0, 0, 0, 180)));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }

        private void drawCoverImage(Graphics2D g, BufferedImage imageAsset) {
            int imageWidth = imageAsset.getWidth();
            int imageHeight = imageAsset.getHeight();
            double scale = Math.max(getWidth() / (double) imageWidth, getHeight() / (double) imageHeight);
            int drawWidth = (int) Math.ceil(imageWidth * scale);
            int drawHeight = (int) Math.ceil(imageHeight * scale);
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;

            g.drawImage(imageAsset, x, y, drawWidth, drawHeight, this);
        }

        private void drawLaneGuides(Graphics2D g) {
            double playfieldWidth = PLAYFIELD_RIGHT - PLAYFIELD_LEFT;
            g.setColor(new Color(255, 255, 255, 20));
            g.fill(new RoundRectangle2D.Double(PLAYFIELD_LEFT, HIT_Y - 7, playfieldWidth, 14, 16, 16));

            g.setColor(NoteRenderer.withOpacity(theme.getAccentColor(), 0.9));
            g.setStroke(new BasicStroke(3f));
            g.drawLine((int) PLAYFIELD_LEFT, (int) HIT_Y, (int) PLAYFIELD_RIGHT, (int) HIT_Y);

            for (int lane = 0; lane < 4; lane++) {
                double x = LANE_X[lane] + 52;
                g.setColor(NoteRenderer.withOpacity(theme.laneColor(lane), 0.25));
                g.setStroke(new BasicStroke(2f));
                g.drawLine((int) x, 105, (int) x, (int) HIT_Y + 50);
            }
        }

        private void drawActiveNotes(Graphics2D g) {
            for (NoteVisual noteVisual : activeNotes) {
                Note note = noteVisual.note();
                NoteRenderer.drawNote(g, noteVisual.x(), noteVisual.y(), note, noteSpeed, theme, keyHeld[note.getLane()]);
            }
        }

        private void drawReceptors(Graphics2D g) {
            for (int lane = 0; lane < 4; lane++) {
                NoteRenderer.drawReceptor(g, LANE_X[lane] + 52, 525, lane, theme, keyHeld[lane]);
            }
        }

        private void drawHud(Graphics2D g) {
            drawCenteredTextFittedInRange(
                    g,
                    playableStage.getTitle().toUpperCase(Locale.ROOT),
                    new Font("Georgia", Font.BOLD, 22),
                    theme.getAccentColor(),
                    42,
                    180,
                    820
            );

            drawRightText(
                    g,
                    "Score " + scoreTracker.getScore(),
                    new Font("Arial", Font.BOLD, 15),
                    NoteRenderer.withOpacity(theme.getSoftTextColor(), 0.72),
                    970,
                    42
            );

            int comboSize = comboPulseSize();
            g.setFont(new Font("Arial", Font.BOLD, comboSize));
            g.setColor(scoreTracker.getCombo() >= 25 ? new Color(255, 215, 0) : Color.WHITE);
            g.drawString("Combo " + scoreTracker.getCombo(), 32, 560);

            drawRightText(
                    g,
                    String.format("%.1f%%", scoreTracker.getCurrentAccuracy()),
                    new Font("Arial", Font.BOLD, 22),
                    theme.getSoftTextColor(),
                    968,
                    560
            );

            g.setColor(new Color(255, 255, 255, 46));
            g.fill(new RoundRectangle2D.Double(330, 62, 340, 5, 8, 8));

            double progress = chartEndTime <= 0 ? 0 : Math.max(0, Math.min(currentChartTime / chartEndTime, 1.0));
            g.setColor(theme.getAccentColor());
            g.fill(new RoundRectangle2D.Double(330, 62, 340 * progress, 5, 8, 8));

            drawCenteredText(
                    g,
                    formatTime(currentChartTime) + " / " + formatTime(chartEndTime),
                    new Font("Arial", Font.BOLD, 12),
                    NoteRenderer.withOpacity(theme.getSoftTextColor(), 0.78),
                    85
            );

            drawTimedCenterText(
                    g,
                    judgmentText,
                    judgmentColor,
                    new Font("Arial", Font.BOLD, 46),
                    judgmentStartedAt,
                    GameplayFeedback.JUDGMENT_FADE_MILLIS,
                    315
            );
            drawTimedCenterText(
                    g,
                    milestoneText,
                    milestoneColor,
                    new Font("Georgia", Font.BOLD, 36),
                    milestoneStartedAt,
                    GameplayFeedback.STAGE_BANNER_MILLIS,
                    210
            );
        }

        private void drawCountdown(Graphics2D g) {
            if (!countdownVisible) {
                return;
            }

            drawCenteredText(
                    g,
                    countdownText,
                    new Font("Georgia", Font.BOLD, 82),
                    new Color(218, 165, 32),
                    310
            );
        }

        private void drawPauseOverlay(Graphics2D g) {
            if (!paused) {
                return;
            }

            int shakeOffset = currentShakeOffset();
            g.translate(-shakeOffset, 0);

            g.setColor(new Color(0, 0, 0, 190));
            g.fillRect(0, 0, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);

            drawCenteredText(
                    g,
                    resumeCountdownActive ? "RESUMING" : "PAUSED",
                    new Font("Georgia", Font.BOLD, 48),
                    new Color(218, 165, 32),
                    195
            );

            if (resumeCountdownActive) {
                drawCenteredText(
                        g,
                        resumeCountdownText(),
                        new Font("Georgia", Font.BOLD, 82),
                        new Color(255, 238, 184),
                        318
                );
            }

            g.translate(shakeOffset, 0);
        }

        private void drawEndFadeOverlay(Graphics2D g) {
            double alpha = endFadeAlpha();
            if (alpha <= 0) {
                return;
            }

            int overlayAlpha = (int) Math.round(220 * alpha);
            g.setColor(new Color(0, 0, 0, overlayAlpha));
            g.fillRect(-currentShakeOffset(), 0, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);
        }

        private void drawHitBursts(Graphics2D g) {
            long now = System.nanoTime();

            for (GameplayEffects.HitBurst burst : effects.hitBursts()) {
                double age = (now - burst.startedAt()) / 1_000_000_000.0;
                double duration = burst.gold() ? 0.46 : 0.26;
                double t = Math.min(1, age / duration);
                double alpha = 1.0 - t;
                double radius = burst.gold() ? 28 + 78 * t : (burst.perfect() ? 22 : 18) + 58 * t;

                if (!burst.gold()) {
                    g.setColor(NoteRenderer.withOpacity(burst.color(), burst.perfect() ? 0.34 * alpha : 0.22 * alpha));
                    g.fill(new RoundRectangle2D.Double(burst.x() - 46 - 35 * t, 496, 92 + 70 * t, 8, 14, 14));
                }

                g.setColor(NoteRenderer.withOpacity(burst.color(), alpha));
                g.setStroke(new BasicStroke(burst.perfect() ? 4f : 3f));
                g.draw(new Ellipse2D.Double(burst.x() - radius, 500 - radius * 0.35, radius * 2, radius * 0.7));

                if (burst.perfect() && !burst.gold()) {
                    g.setColor(NoteRenderer.withOpacity(Color.WHITE, 0.8 * alpha));
                    g.setStroke(new BasicStroke(2f));
                    double perfectRadius = 10 + 42 * t;
                    g.draw(new Ellipse2D.Double(
                            burst.x() - perfectRadius,
                            500 - perfectRadius * 0.45,
                            perfectRadius * 2,
                            perfectRadius * 0.9
                    ));
                }
            }
        }

        private void drawBeatPulses(Graphics2D g) {
            long now = System.nanoTime();

            for (GameplayEffects.BeatPulse pulse : effects.beatPulses()) {
                double duration = pulse.measureStart() ? 0.36 : 0.25;
                double t = Math.min(1, ((now - pulse.startedAt()) / 1_000_000_000.0) / duration);
                double alpha = (pulse.measureStart() ? 0.48 : 0.28) * (1.0 - t);
                double radius = (pulse.measureStart() ? 42 : 24) * (1.0 + (pulse.measureStart() ? 6.8 : 4.2) * t);

                g.setColor(NoteRenderer.withOpacity(theme.getPulseColor(), alpha));
                g.setStroke(new BasicStroke(pulse.measureStart() ? 3f : 2f));
                g.draw(new Ellipse2D.Double(500 - radius, 500 - radius * 0.3, radius * 2, radius * 0.6));
            }
        }

        private int comboPulseSize() {
            long elapsed = System.nanoTime() - comboPulseStartedAt;
            if (elapsed < 0 || elapsed > COMBO_PULSE_NANOS) {
                return 22;
            }

            double t = 1.0 - (elapsed / (double) COMBO_PULSE_NANOS);
            return (int) Math.round(22 + (4 * t));
        }

        private int currentShakeOffset() {
            long elapsed = System.nanoTime() - shakeStartedAt;
            if (elapsed < 0 || elapsed > SHAKE_NANOS) {
                return 0;
            }

            int frame = (int) (elapsed / 20_000_000L);
            return frame % 2 == 0 ? -5 : 5;
        }

        private void drawTimedCenterText(
                Graphics2D g,
                String text,
                Color color,
                Font font,
                long startedAt,
                int durationMillis,
                int y
        ) {
            if (text == null || text.isBlank() || startedAt == 0) {
                return;
            }

            double ageMillis = (System.nanoTime() - startedAt) / 1_000_000.0;
            if (ageMillis > durationMillis) {
                return;
            }

            double alpha = Math.max(0.15, 1.0 - (ageMillis / durationMillis));
            drawCenteredText(g, text, font, NoteRenderer.withOpacity(color, alpha), y);
        }

        private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int baselineY) {
            g.setFont(font);
            g.setColor(color);
            FontMetrics metrics = g.getFontMetrics();
            int x = (GameConfig.SCENE_WIDTH - metrics.stringWidth(text)) / 2;
            g.drawString(text, x, baselineY);
        }

        private void drawCenteredTextFitted(Graphics2D g, String text, Font font, Color color, int baselineY, int maxWidth) {
            Font fittedFont = font;
            g.setFont(fittedFont);
            while (g.getFontMetrics().stringWidth(text) > maxWidth && fittedFont.getSize() > 14) {
                fittedFont = fittedFont.deriveFont((float) fittedFont.getSize() - 1f);
                g.setFont(fittedFont);
            }

            drawCenteredText(g, text, fittedFont, color, baselineY);
        }

        private void drawCenteredTextFittedInRange(
                Graphics2D g,
                String text,
                Font font,
                Color color,
                int baselineY,
                int leftX,
                int rightX
        ) {
            Font fittedFont = font;
            int maxWidth = rightX - leftX;
            g.setFont(fittedFont);
            while (g.getFontMetrics().stringWidth(text) > maxWidth && fittedFont.getSize() > 14) {
                fittedFont = fittedFont.deriveFont((float) fittedFont.getSize() - 1f);
                g.setFont(fittedFont);
            }

            FontMetrics metrics = g.getFontMetrics();
            int x = leftX + (maxWidth - metrics.stringWidth(text)) / 2;
            g.setColor(color);
            g.drawString(text, x, baselineY);
        }

        private void drawRightText(Graphics2D g, String text, Font font, Color color, int rightX, int baselineY) {
            g.setFont(font);
            g.setColor(color);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, rightX - metrics.stringWidth(text), baselineY);
        }
    }

    private static final class NoteVisual {
        private final Note note;
        private final double x;
        private double y;

        private NoteVisual(Note note, double x, double y) {
            this.note = note;
            this.x = x;
            this.y = y;
        }

        private Note note() {
            return note;
        }

        private double x() {
            return x;
        }

        private double y() {
            return y;
        }

        private void setY(double y) {
            this.y = y;
        }

        private double centerX() {
            return x + 52;
        }
    }
}
