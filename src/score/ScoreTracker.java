package score;

public class ScoreTracker {
    public enum Judgment {
        PERFECT(300, true),
        GREAT(200, true),
        GOOD(100, true),
        BAD(50, false),
        MISS(0, false);

        private final int points;
        private final boolean keepsCombo;

        Judgment(int points, boolean keepsCombo) {
            this.points = points;
            this.keepsCombo = keepsCombo;
        }

        public int getPoints() {
            return points;
        }

        public boolean keepsCombo() {
            return keepsCombo;
        }
    }

    private final int totalNotes;
    private int judgmentScore;
    private int bonusScore;
    private int combo;
    private int maxCombo;
    private int perfectCount;
    private int greatCount;
    private int goodCount;
    private int badCount;
    private int missCount;
    private int goldHitCount;
    private int goldMissCount;
    private int timedHitCount;
    private int earlyHitCount;
    private int lateHitCount;
    private double signedTimingOffsetTotal;
    private double absoluteTimingOffsetTotal;

    public ScoreTracker(int totalNotes) {
        this.totalNotes = Math.max(totalNotes, 1);
    }

    public void record(Judgment judgment) {
        judgmentScore += judgment.getPoints();

        if (judgment.keepsCombo()) {
            combo++;
            maxCombo = Math.max(maxCombo, combo);
        } else {
            combo = 0;
        }

        switch (judgment) {
            case PERFECT -> perfectCount++;
            case GREAT -> greatCount++;
            case GOOD -> goodCount++;
            case BAD -> badCount++;
            case MISS -> missCount++;
        }
    }

    public void recordHoldTick() {
        bonusScore += 20;
    }

    public void recordHoldReleaseBonus(Judgment judgment) {
        bonusScore += switch (judgment) {
            case PERFECT -> 120;
            case GREAT -> 90;
            case GOOD -> 60;
            case BAD -> 20;
            case MISS -> 0;
        };
    }

    public void recordGoldNoteBonus(Judgment judgment) {
        if (judgment == Judgment.MISS) {
            goldMissCount++;
        } else {
            goldHitCount++;
        }

        bonusScore += switch (judgment) {
            case PERFECT -> 180;
            case GREAT -> 140;
            case GOOD -> 90;
            case BAD -> 25;
            case MISS -> 0;
        };
    }

    public void recordTimingOffset(double signedOffsetSeconds) {
        timedHitCount++;
        signedTimingOffsetTotal += signedOffsetSeconds;
        absoluteTimingOffsetTotal += Math.abs(signedOffsetSeconds);

        if (signedOffsetSeconds < -0.005) {
            earlyHitCount++;
        } else if (signedOffsetSeconds > 0.005) {
            lateHitCount++;
        }
    }

    public int getScore() {
        return judgmentScore + bonusScore;
    }

    public int getBonusScore() {
        return bonusScore;
    }

    public int getCombo() {
        return combo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public int getPerfectCount() {
        return perfectCount;
    }

    public int getGreatCount() {
        return greatCount;
    }

    public int getGoodCount() {
        return goodCount;
    }

    public int getBadCount() {
        return badCount;
    }

    public int getMissCount() {
        return missCount;
    }

    public int getGoldHitCount() {
        return goldHitCount;
    }

    public int getGoldMissCount() {
        return goldMissCount;
    }

    public int getTimedHitCount() {
        return timedHitCount;
    }

    public int getEarlyHitCount() {
        return earlyHitCount;
    }

    public int getLateHitCount() {
        return lateHitCount;
    }

    public double getAverageSignedTimingOffsetMillis() {
        if (timedHitCount == 0) {
            return 0;
        }

        return (signedTimingOffsetTotal / timedHitCount) * 1000.0;
    }

    public double getAverageAbsoluteTimingOffsetMillis() {
        if (timedHitCount == 0) {
            return 0;
        }

        return (absoluteTimingOffsetTotal / timedHitCount) * 1000.0;
    }

    public int getTotalJudged() {
        return perfectCount + greatCount + goodCount + badCount + missCount;
    }

    public int getTotalNotes() {
        return totalNotes;
    }

    public double getAccuracy() {
        return (judgmentScore / (totalNotes * 300.0)) * 100.0;
    }

    public double getCurrentAccuracy() {
        int judged = getTotalJudged();

        if (judged == 0) {
            return 100.0;
        }

        return (judgmentScore / (judged * 300.0)) * 100.0;
    }

    public String getGrade() {
        double accuracy = getAccuracy();

        if (accuracy >= 95) return "S";
        if (accuracy >= 90) return "A";
        if (accuracy >= 80) return "B";
        if (accuracy >= 70) return "C";
        if (accuracy >= 60) return "D";
        return "F";
    }
}
