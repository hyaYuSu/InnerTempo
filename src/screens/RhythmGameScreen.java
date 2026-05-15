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
import ui.GameUiFactory;
import ui.GameplayFeedback;
import ui.JudgmentStyle;
import ui.JourneyTheme;
import ui.NoteRenderer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
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
    private static final long COMBO_PULSE_NANOS = 140_000_000L;
    private static final long SHAKE_NANOS = 160_000_000L;

    private final ScreenManager controller;
    private final GameplaySettings options;
    private final RandomChartGenerator chartGenerator;
    private final PlayableStage playableStage;
    private final HitJudge hitJudge;
    private final JourneyTheme theme;
    private final StageBeatProfile beatProfile;

    private final List<NoteVisual> activeNotes = new ArrayList<>();
    private final List<HitBurst> hitBursts = new ArrayList<>();
    private final List<BeatPulse> beatPulses = new ArrayList<>();
    private final boolean[] keyHeld = new boolean[4];

    private GamePanel root;
    private JButton backButton;
    private final List<JButton> pauseButtons = new ArrayList<>();
    private List<Note> noteData;
    private ScoreTracker scoreTracker;
    private StageMusicPlayer musicPlayer;
    private Timer timer;
    private long startTime;
    private long pauseStartedAt;
    private long judgmentStartedAt;
    private long milestoneStartedAt;
    private long comboPulseStartedAt;
    private long shakeStartedAt;
    private boolean paused;
    private boolean countdownComplete;
    private boolean musicStarted;
    private boolean finished;
    private int lastBeatIndex = -1;
    private double chartEndTime;
    private double noteSpeed;
    private double currentChartTime;
    private String countdownText = "3";
    private boolean countdownVisible = true;
    private String debugText = "";
    private boolean debugVisible;
    private String judgmentText = "";
    private Color judgmentColor = Color.WHITE;
    private String milestoneText = "";
    private Color milestoneColor = Color.WHITE;
    private String pauseStatsText = "";

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

    public JPanel create() {
        root = new GamePanel();
        root.setLayout(null);
        root.setFocusable(true);

        noteData = chartGenerator.generateChart(playableStage);
        scoreTracker = new ScoreTracker(noteData.size());
        musicPlayer = new StageMusicPlayer(playableStage);
        chartEndTime = getChartEndTime(noteData) + 0.6;
        noteSpeed = GameConfig.BASE_NOTE_SPEED * options.getNoteSpeedMultiplier() * stageSpeedMultiplier();
        currentChartTime = 0;

        createControls();
        installKeyBindings(root);

        startTime = System.nanoTime();
        timer = new Timer(16, e -> tick());
        timer.start();

        return root;
    }

    private void createControls() {
        backButton = GameUiFactory.createSmallButton("BACK");
        backButton.setBounds(30, 25, 120, 40);
        backButton.addActionListener(e -> {
            timer.stop();
            stopMusic();
            controller.showJourneySelect(playableStage.getJourneyId());
        });
        root.add(backButton);

        JButton resumeButton = GameUiFactory.createSmallButton("RESUME");
        JButton retryButton = GameUiFactory.createSmallButton("RETRY");
        JButton playButton = GameUiFactory.createSmallButton("STAGES");
        JButton optionsButton = GameUiFactory.createSmallButton("OPTIONS");

        resumeButton.addActionListener(e -> resumeGame());
        retryButton.addActionListener(e -> {
            timer.stop();
            stopMusic();
            controller.startStage(playableStage);
        });
        playButton.addActionListener(e -> {
            timer.stop();
            stopMusic();
            controller.showJourneySelect(playableStage.getJourneyId());
        });
        optionsButton.addActionListener(e -> {
            timer.stop();
            stopMusic();
            controller.showOptions();
        });

        int buttonX = (GameConfig.SCENE_WIDTH - 120) / 2;
        int y = 300;
        JButton[] buttons = {resumeButton, retryButton, playButton, optionsButton};
        for (JButton button : buttons) {
            button.setBounds(buttonX, y, 120, 40);
            button.setVisible(false);
            pauseButtons.add(button);
            root.add(button);
            y += 56;
        }
    }

    private void installKeyBindings(JComponent component) {
        bindPress(component, KeyEvent.VK_LEFT);
        bindPress(component, KeyEvent.VK_UP);
        bindPress(component, KeyEvent.VK_DOWN);
        bindPress(component, KeyEvent.VK_RIGHT);
        bindPress(component, KeyEvent.VK_ESCAPE);
        bindPress(component, KeyEvent.VK_OPEN_BRACKET);
        bindPress(component, KeyEvent.VK_CLOSE_BRACKET);
        bindPress(component, KeyEvent.VK_0);

        bindRelease(component, KeyEvent.VK_LEFT);
        bindRelease(component, KeyEvent.VK_UP);
        bindRelease(component, KeyEvent.VK_DOWN);
        bindRelease(component, KeyEvent.VK_RIGHT);
    }

    private void bindPress(JComponent component, int keyCode) {
        String actionKey = "pressed-" + keyCode;
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, false), actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleKeyPressed(keyCode);
            }
        });
    }

    private void bindRelease(JComponent component, int keyCode) {
        String actionKey = "released-" + keyCode;
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, true), actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleKeyReleased(keyCode);
            }
        });
    }

    private void tick() {
        pruneEffects();

        double rawTime = rawSecondsSinceStart();
        updateCountdown(rawTime);

        if (rawTime < COUNTDOWN_SECONDS) {
            root.repaint();
            return;
        }

        countdownComplete = true;
        startMusicIfNeeded();
        double audioTime = gameplaySeconds();
        double currentTime = chartSeconds(audioTime);
        currentChartTime = currentTime;

        updateBeatPulse(audioTime);
        updateDebugOverlay(audioTime, currentTime);
        spawnNotes(noteData, currentTime, noteSpeed);
        updateActiveNotes(currentTime, noteSpeed);

        if (!finished && currentTime >= chartEndTime) {
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

            noteVisual.setY(HIT_Y - ((note.getTime() - currentTime) * pixelsPerSecond));

            if (note.isHolding() && updateHeldNote(noteVisual, note, currentTime, iterator)) {
                continue;
            }

            if (!note.isHit() && !note.isHolding() && currentTime > note.getTime() + hitJudge.badWindow()) {
                recordAndRemove(noteVisual, iterator, ScoreTracker.Judgment.MISS, "MISS", false, null);
            }
        }
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
                    note.getReleasedAt() + options.getInputOffsetSeconds(),
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
        if (handleCalibrationKey(code)) {
            return;
        }

        if (code == KeyEvent.VK_ESCAPE) {
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

        if (resumeGraceHold(lane)) {
            return;
        }

        checkArrowHit(lane, chartSeconds());
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
        releaseHeldNote(lane, chartSeconds());
        root.repaint();
    }

    private void checkArrowHit(int lane, double currentTime) {
        double adjustedTime = currentTime + options.getInputOffsetSeconds();
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
        double adjustedTime = currentTime + options.getInputOffsetSeconds();
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

    private boolean handleCalibrationKey(int code) {
        if (code == KeyEvent.VK_OPEN_BRACKET) {
            options.adjustStageChartOffsetMillis(playableStage, -10);
            showCalibrationOffset();
            return true;
        }

        if (code == KeyEvent.VK_CLOSE_BRACKET) {
            options.adjustStageChartOffsetMillis(playableStage, 10);
            showCalibrationOffset();
            return true;
        }

        if (code == KeyEvent.VK_0) {
            options.resetStageChartOffset(playableStage);
            showCalibrationOffset();
            return true;
        }

        return false;
    }

    private void showCalibrationOffset() {
        showStageBanner(
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
        }

        updatePauseStats();
        setPauseControlsVisible(true);
        root.repaint();
    }

    private void resumeGame() {
        if (!paused) {
            return;
        }

        startTime += System.nanoTime() - pauseStartedAt;
        paused = false;
        setPauseControlsVisible(false);
        if (musicStarted) {
            musicPlayer.resume();
        }
        timer.start();
    }

    private void setPauseControlsVisible(boolean visible) {
        backButton.setVisible(!visible);
        for (JButton button : pauseButtons) {
            button.setVisible(visible);
        }
    }

    private void updatePauseStats() {
        pauseStatsText =
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
                        + String.format("%+dms", Math.round(stageChartOffsetSeconds() * 1000));
    }

    private int laneForKey(int code) {
        return switch (code) {
            case KeyEvent.VK_LEFT -> 0;
            case KeyEvent.VK_UP -> 1;
            case KeyEvent.VK_DOWN -> 2;
            case KeyEvent.VK_RIGHT -> 3;
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
            showStageBanner("FULL COMBO", new Color(255, 215, 0));

            Timer delay = new Timer(900, e -> controller.showStageResultStory(playableStage, scoreTracker));
            delay.setRepeats(false);
            delay.start();
            root.repaint();
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
        beatPulses.add(new BeatPulse(System.nanoTime(), beatIndex % beatProfile.beatsPerMeasure() == 0));
    }

    private void updateDebugOverlay(double audioTime, double currentTime) {
        if (beatProfile == null) {
            debugText = String.format("Audio %.3f | Chart %.3f", audioTime, currentTime);
            return;
        }

        if (currentTime < beatProfile.firstBeatOffset()) {
            debugText = String.format(
                    "Audio %.3f | Chart %.3f | Offset %+dms | waiting for first beat",
                    audioTime,
                    currentTime,
                    Math.round(stageChartOffsetSeconds() * 1000)
            );
            return;
        }

        int slotsPerMeasure = beatProfile.beatsPerMeasure() * beatProfile.gridSlotsPerBeat();
        int totalSlot = (int) Math.floor((currentTime - beatProfile.firstBeatOffset()) / beatProfile.gridSlotSeconds());
        int measure = totalSlot / slotsPerMeasure;
        int slot = totalSlot % slotsPerMeasure;
        int beat = (slot / beatProfile.gridSlotsPerBeat()) + 1;
        int beatSlot = slot % beatProfile.gridSlotsPerBeat();

        debugText = String.format(
                "Audio %.3f | Chart %.3f | M %02d B %d Slot %02d (%d/%d) | Offset %+dms",
                audioTime,
                currentTime,
                measure + 1,
                beat,
                slot,
                beatSlot,
                beatProfile.gridSlotsPerBeat(),
                Math.round(stageChartOffsetSeconds() * 1000)
        );
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

    private void stopMusic() {
        musicPlayer.stop();
        musicPlayer.dispose();
    }

    private void createHitBurst(NoteVisual noteVisual, ScoreTracker.Judgment judgment) {
        hitBursts.add(new HitBurst(
                System.nanoTime(),
                noteVisual.centerX(),
                JudgmentStyle.color(judgment),
                judgment == ScoreTracker.Judgment.PERFECT,
                false
        ));
    }

    private void createGoldBurst(NoteVisual noteVisual) {
        hitBursts.add(new HitBurst(System.nanoTime(), noteVisual.centerX(), theme.getGoldNoteColor(), true, true));
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

    private void pruneEffects() {
        long now = System.nanoTime();
        hitBursts.removeIf(effect -> now - effect.startedAt() > 500_000_000L);
        beatPulses.removeIf(effect -> now - effect.startedAt() > 380_000_000L);
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
            drawCenteredText(
                    g,
                    "Stage " + playableStage.getNumber() + ": " + playableStage.getTitle(),
                    new Font("Georgia", Font.BOLD, 22),
                    theme.getAccentColor(),
                    42
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

            if (debugVisible) {
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.setColor(NoteRenderer.withOpacity(theme.getSoftTextColor(), 0.78));
                g.drawString(debugText, 30, 585);
            }

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

            g.setColor(new Color(0, 0, 0, 210));
            g.fillRect(-currentShakeOffset(), 0, GameConfig.SCENE_WIDTH, GameConfig.SCENE_HEIGHT);

            drawCenteredText(
                    g,
                    "PAUSED",
                    new Font("Georgia", Font.BOLD, 48),
                    new Color(218, 165, 32),
                    195
            );

            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(Color.LIGHT_GRAY);
            String[] lines = pauseStatsText.split("\\n");
            int y = 240;
            for (String line : lines) {
                drawCenteredText(g, line, g.getFont(), Color.LIGHT_GRAY, y);
                y += 26;
            }
        }

        private void drawHitBursts(Graphics2D g) {
            long now = System.nanoTime();

            for (HitBurst burst : hitBursts) {
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

            for (BeatPulse pulse : beatPulses) {
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

        private void drawRightText(Graphics2D g, String text, Font font, Color color, int rightX, int baselineY) {
            g.setFont(font);
            g.setColor(color);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, rightX - metrics.stringWidth(text), baselineY);
        }
    }

    private record HitBurst(long startedAt, double x, Color color, boolean perfect, boolean gold) {
    }

    private record BeatPulse(long startedAt, boolean measureStart) {
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
