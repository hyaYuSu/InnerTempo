package ui;

import score.ScoreTracker;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public final class GameplayFeedback {
    private GameplayFeedback() {
    }

    public static void showJudgment(Text judgmentText, String text, Color color) {
        judgmentText.setText(text);
        judgmentText.setFill(color);
        judgmentText.setOpacity(1);
        judgmentText.setScaleY(1.1);
        judgmentText.setScaleX(1.1);

        FadeTransition fade = new FadeTransition(Duration.millis(550), judgmentText);
        fade.setFromValue(1);
        fade.setToValue(0.25);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), judgmentText);
        scale.setFromX(1.1);
        scale.setFromY(1.1);
        scale.setToX(1);
        scale.setToY(1);

        new ParallelTransition(fade, scale).play();
    }

    public static void createHitBurst(Pane root, Group noteGroup, ScoreTracker.Judgment judgment) {
        Bounds bounds = noteGroup.getBoundsInParent();
        Color judgmentColor = JudgmentStyle.color(judgment);

        Rectangle lineFlash = new Rectangle(bounds.getCenterX() - 46, 496, 92, 8);
        lineFlash.setFill(Color.color(
                judgmentColor.getRed(),
                judgmentColor.getGreen(),
                judgmentColor.getBlue(),
                judgment == ScoreTracker.Judgment.PERFECT ? 0.34 : 0.22
        ));
        lineFlash.setArcWidth(14);
        lineFlash.setArcHeight(14);
        root.getChildren().add(lineFlash);

        FadeTransition flashFade = new FadeTransition(Duration.millis(180), lineFlash);
        flashFade.setFromValue(1);
        flashFade.setToValue(0);

        ScaleTransition flashScale = new ScaleTransition(Duration.millis(180), lineFlash);
        flashScale.setToX(judgment == ScoreTracker.Judgment.PERFECT ? 1.9 : 1.45);
        flashScale.setToY(0.65);

        ParallelTransition flash = new ParallelTransition(flashFade, flashScale);
        flash.setOnFinished(e -> root.getChildren().remove(lineFlash));
        flash.play();

        Circle burst = new Circle(bounds.getCenterX(), 500, burstRadius(judgment));
        burst.setFill(Color.TRANSPARENT);
        burst.setStroke(judgmentColor);
        burst.setStrokeWidth(judgment == ScoreTracker.Judgment.PERFECT ? 4 : 3);
        root.getChildren().add(burst);

        FadeTransition fade = new FadeTransition(Duration.millis(260), burst);
        fade.setFromValue(0.9);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(260), burst);
        scale.setToX(judgment == ScoreTracker.Judgment.PERFECT ? 3.8 : 2.8);
        scale.setToY(judgment == ScoreTracker.Judgment.PERFECT ? 0.85 : 0.7);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setOnFinished(e -> root.getChildren().remove(burst));
        animation.play();

        if (judgment == ScoreTracker.Judgment.PERFECT) {
            createPerfectRing(root, bounds.getCenterX());
        }
    }

    public static void createGoldBurst(Pane root, Group noteGroup, JourneyTheme theme) {
        Bounds bounds = noteGroup.getBoundsInParent();
        Circle burst = new Circle(bounds.getCenterX(), 500, 28);
        burst.setFill(Color.rgb(255, 215, 80, 0.18));
        burst.setStroke(theme.getGoldNoteColor());
        burst.setStrokeWidth(5);
        root.getChildren().add(burst);

        FadeTransition fade = new FadeTransition(Duration.millis(440), burst);
        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(460), burst);
        scale.setToX(3.2);
        scale.setToY(3.2);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setOnFinished(e -> root.getChildren().remove(burst));
        animation.play();
    }

    public static void setReceptorPressed(Group receptor, boolean pressed, Color laneColor) {
        NoteRenderer.setArrowFill(receptor, pressed ? laneColor : Color.WHITE, pressed ? Color.WHITE : laneColor, pressed);
        receptor.setScaleX(pressed ? 1.16 : 1);
        receptor.setScaleY(pressed ? 1.16 : 1);

        DropShadow glow = new DropShadow();
        glow.setColor(laneColor);
        glow.setRadius(pressed ? 34 : 18);
        glow.setSpread(pressed ? 0.38 : 0.18);
        receptor.setEffect(glow);
    }

    public static void pulseCombo(Text comboText, int combo) {
        if (combo <= 1) {
            return;
        }

        comboText.setFill(combo >= 25 ? Color.GOLD : Color.WHITE);
        comboText.setScaleX(1.18);
        comboText.setScaleY(1.18);

        ScaleTransition scale = new ScaleTransition(Duration.millis(140), comboText);
        scale.setFromX(1.18);
        scale.setFromY(1.18);
        scale.setToX(1);
        scale.setToY(1);
        scale.play();
    }

    public static void shake(Pane root) {
        root.setTranslateX(-5);

        TranslateTransition shake = new TranslateTransition(Duration.millis(20), root);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> root.setTranslateX(0));
        shake.play();
    }

    public static void showStageBanner(Text bannerText, String text, Color color) {
        bannerText.setText(text);
        bannerText.setFill(color);
        bannerText.setOpacity(1);
        bannerText.setScaleX(0.8);
        bannerText.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(Duration.millis(900), bannerText);
        fade.setFromValue(1);
        fade.setToValue(0.15);

        ScaleTransition scale = new ScaleTransition(Duration.millis(320), bannerText);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.18);
        scale.setToY(1.18);

        new ParallelTransition(fade, scale).play();
    }

    public static void pulseBeat(Pane root, Group[] receptors, JourneyTheme theme, boolean measureStart) {
        double radius = measureStart ? 42 : 24;
        Circle pulse = new Circle(500, 500, radius);
        pulse.setFill(Color.TRANSPARENT);
        pulse.setStroke(theme.getPulseColor());
        pulse.setStrokeWidth(measureStart ? 3 : 2);
        pulse.setOpacity(measureStart ? 0.48 : 0.28);
        root.getChildren().add(pulse);

        FadeTransition fade = new FadeTransition(Duration.millis(measureStart ? 360 : 250), pulse);
        fade.setFromValue(pulse.getOpacity());
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(measureStart ? 360 : 250), pulse);
        scale.setToX(measureStart ? 7.8 : 5.2);
        scale.setToY(measureStart ? 0.58 : 0.42);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setOnFinished(e -> root.getChildren().remove(pulse));
        animation.play();

        for (Group receptor : receptors) {
            if (receptor == null) {
                continue;
            }

            ScaleTransition receptorPulse = new ScaleTransition(Duration.millis(130), receptor);
            receptorPulse.setFromX(measureStart ? 1.10 : 1.04);
            receptorPulse.setFromY(measureStart ? 1.10 : 1.04);
            receptorPulse.setToX(1);
            receptorPulse.setToY(1);
            receptorPulse.play();
        }
    }

    private static void createPerfectRing(Pane root, double x) {
        Circle ring = new Circle(x, 500, 10);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.WHITE);
        ring.setStrokeWidth(2);
        root.getChildren().add(ring);

        FadeTransition fade = new FadeTransition(Duration.millis(360), ring);
        fade.setFromValue(0.8);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(360), ring);
        scale.setToX(4.2);
        scale.setToY(0.9);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.setOnFinished(e -> root.getChildren().remove(ring));
        animation.play();
    }

    private static double burstRadius(ScoreTracker.Judgment judgment) {
        return switch (judgment) {
            case PERFECT -> 22;
            case GREAT -> 19;
            case GOOD -> 16;
            case BAD -> 14;
            case MISS -> 12;
        };
    }
}
