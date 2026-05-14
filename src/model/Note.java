package model;

import score.ScoreTracker;

public class Note {
    private final double time;
    private final int lane;
    private final double holdDuration;
    private final NoteType type;
    private boolean hit;
    private boolean spawned;
    private boolean holding;
    private double holdProgress;
    private double holdStartedAt;
    private double releasedAt;
    private double nextHoldTickTime;
    private boolean releaseGraceActive;
    private boolean holdDropped;
    private ScoreTracker.Judgment holdStartJudgment;
    private double holdStartOffsetSeconds;

    public Note(double time, int lane) {
        this(time, lane, 0);
    }

    public Note(double time, int lane, double holdDuration) {
        this(time, lane, holdDuration, NoteType.NORMAL);
    }

    public Note(double time, int lane, NoteType type) {
        this(time, lane, 0, type);
    }

    public Note(double time, int lane, double holdDuration, NoteType type) {
        this.time = time;
        this.lane = lane;
        this.holdDuration = holdDuration;
        this.type = type;
        this.hit = false;
        this.spawned = false;
        this.holding = false;
        this.holdProgress = 0;
        this.holdStartedAt = time;
        this.releasedAt = 0;
        this.nextHoldTickTime = time;
        this.releaseGraceActive = false;
        this.holdDropped = false;
    }

    public double getTime() {
        return time;
    }

    public int getLane() {
        return lane;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }

    public double getHoldDuration() {
        return holdDuration;
    }

    public NoteType getType() {
        return type;
    }

    public boolean isGoldNote() {
        return type == NoteType.GOLD;
    }

    public boolean isHoldNote() {
        return holdDuration > 0;
    }

    public boolean isHolding() {
        return holding;
    }

    public void setHolding(boolean holding) {
        this.holding = holding;
    }

    public double getHoldProgress() {
        return holdProgress;
    }

    public void setHoldProgress(double holdProgress) {
        this.holdProgress = holdProgress;
    }

    public double getHoldStartedAt() {
        return holdStartedAt;
    }

    public void setHoldStartedAt(double holdStartedAt) {
        this.holdStartedAt = holdStartedAt;
    }

    public double getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(double releasedAt) {
        this.releasedAt = releasedAt;
    }

    public double getNextHoldTickTime() {
        return nextHoldTickTime;
    }

    public void setNextHoldTickTime(double nextHoldTickTime) {
        this.nextHoldTickTime = nextHoldTickTime;
    }

    public boolean isReleaseGraceActive() {
        return releaseGraceActive;
    }

    public void setReleaseGraceActive(boolean releaseGraceActive) {
        this.releaseGraceActive = releaseGraceActive;
    }

    public boolean isHoldDropped() {
        return holdDropped;
    }

    public void setHoldDropped(boolean holdDropped) {
        this.holdDropped = holdDropped;
    }

    public ScoreTracker.Judgment getHoldStartJudgment() {
        return holdStartJudgment;
    }

    public void setHoldStartJudgment(ScoreTracker.Judgment holdStartJudgment) {
        this.holdStartJudgment = holdStartJudgment;
    }

    public double getHoldStartOffsetSeconds() {
        return holdStartOffsetSeconds;
    }

    public void setHoldStartOffsetSeconds(double holdStartOffsetSeconds) {
        this.holdStartOffsetSeconds = holdStartOffsetSeconds;
    }

    public double getHoldEndTime() {
        return time + holdDuration;
    }
}
