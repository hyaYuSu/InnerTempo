package ui;

import model.Note;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public final class NoteRenderer {
    private static final String HOLD_HEIGHT_KEY = "holdHeight";
    private static final String THEME_KEY = "theme";
    private static final String HOLD_CENTER_X_KEY = "holdCenterX";
    private static final String HOLD_WIDTH_KEY = "holdWidth";
    private static final String HOLD_RATIO_KEY = "holdRatio";
    private static final String BASE_X_KEY = "baseX";
    private static final double ARROW_CENTER_OFFSET = 52;

    private NoteRenderer() {
    }

    public static Group create(double x, Note note, double pixelsPerSecond) {
        return create(x, note, pixelsPerSecond, JourneyTheme.waves());
    }

    public static Group create(double x, Note note, double pixelsPerSecond, JourneyTheme theme) {
        Group group = new Group();
        group.getProperties().put(THEME_KEY, theme);

        if (note.isHoldNote()) {
            double holdHeight = note.getHoldDuration() * pixelsPerSecond;
            Color laneColor = colorForLane(note.getLane(), theme);
            double centerX = x + ARROW_CENTER_OFFSET;
            group.getProperties().put(HOLD_HEIGHT_KEY, holdHeight);

            Path holdBack = createWavePath(centerX, -holdHeight, 0, 68, 8);
            holdBack.setFill(withOpacity(laneColor, 0.12));
            holdBack.setStroke(withOpacity(laneColor, 0.34));
            holdBack.setStrokeWidth(2);
            holdBack.setUserData("body");

            Path holdFill = createWavePath(centerX, -1, 0, 54, 6);
            holdFill.setFill(withOpacity(laneColor, 0.58));
            holdFill.setStroke(withOpacity(Color.WHITE, 0.45));
            holdFill.setStrokeWidth(1.4);
            holdFill.setVisible(false);
            holdFill.setUserData("fill");
            holdFill.getProperties().put(HOLD_CENTER_X_KEY, centerX);
            holdFill.getProperties().put(HOLD_WIDTH_KEY, 54.0);

            Rectangle holdTail = new Rectangle(centerX - 36, -holdHeight - 5, 72, 10);
            holdTail.setFill(withOpacity(laneColor, 0.72));
            holdTail.setArcWidth(12);
            holdTail.setArcHeight(12);
            holdTail.setUserData("tail");

            group.getChildren().addAll(holdBack, holdTail, holdFill);
            addStreamRipples(group, centerX, holdHeight, laneColor);
        }

        group.getChildren().add(createArrowIcon(
                note.getLane(),
                x + ARROW_CENTER_OFFSET,
                0,
                note.isGoldNote() ? 88 : 80,
                noteColor(note, theme),
                note.isGoldNote() ? Color.WHITE : Color.rgb(255, 255, 255, 0.78),
                note.isGoldNote(),
                note.isGoldNote() ? 0.32 : 0.18
        ));
        group.setUserData(note);

        return group;
    }

    public static Group createReceptor(double centerX, double centerY, int lane, JourneyTheme theme) {
        Group receptor = createArrowIcon(
                lane,
                0,
                0,
                74,
                Color.WHITE,
                theme.laneColor(lane),
                false,
                0.08
        );
        receptor.setLayoutX(centerX);
        receptor.setLayoutY(centerY);
        receptor.setUserData("receptor");
        return receptor;
    }

    public static void updateHoldFill(Group noteGroup, Note note) {
        updateHoldVisuals(noteGroup, note, 1.0, true);
    }

    public static void updateHoldVisuals(Group noteGroup, Note note, double graceRatio, boolean keyHeld) {
        double maxHeight = holdHeight(noteGroup, note);
        double progress = Math.min(note.getHoldProgress() / note.getHoldDuration(), 1.0);
        Color holdColor = holdColor(note, keyHeld, themeFor(noteGroup));

        for (Node node : noteGroup.getChildren()) {
            if ("fill".equals(node.getUserData())) {
                updateStreamFill(node, maxHeight, progress, holdColor, graceRatio);
            } else if ("body".equals(node.getUserData())) {
                updateStreamBody(node, holdColor, graceRatio);
            } else if ("tail".equals(node.getUserData()) && node instanceof Shape tail) {
                tail.setFill(withOpacity(holdColor, note.isHolding() ? 0.82 : 0.64));
            } else if ("ripple".equals(node.getUserData()) && node instanceof Circle ripple) {
                updateStreamRipple(ripple, maxHeight, progress, holdColor, graceRatio);
            } else if (note.isHolding()) {
                updateArrowFill(node, holdColor);
            }
        }
    }

    public static void setArrowFill(Group arrowGroup, Color fill, Color stroke, boolean pressed) {
        for (Node node : arrowGroup.getChildren()) {
            updateArrowFill(node, fill);
            updateArrowStroke(node, stroke);
            updateArrowHalo(node, fill, pressed);
        }
    }

    private static Group createArrowIcon(
            int lane,
            double centerX,
            double centerY,
            double size,
            Color fill,
            Color stroke,
            boolean strongGlow,
            double haloOpacity
    ) {
        Group icon = new Group();
        double half = size / 2.0;

        Circle halo = new Circle(centerX, centerY, half * 0.78);
        halo.setFill(withOpacity(fill, haloOpacity));
        halo.setUserData("arrowHalo");

        Polygon arrow = new Polygon(
                centerX, centerY - half,
                centerX + half * 0.58, centerY - half * 0.12,
                centerX + half * 0.24, centerY - half * 0.12,
                centerX + half * 0.24, centerY + half,
                centerX - half * 0.24, centerY + half,
                centerX - half * 0.24, centerY - half * 0.12,
                centerX - half * 0.58, centerY - half * 0.12
        );
        arrow.setFill(fill);
        arrow.setStroke(stroke);
        arrow.setStrokeWidth(strongGlow ? 5 : 3);
        arrow.setUserData("arrowShape");
        arrow.setRotate(rotationForLane(lane));

        DropShadow glow = new DropShadow();
        glow.setColor(fill);
        glow.setRadius(strongGlow ? 34 : 22);
        glow.setSpread(strongGlow ? 0.48 : 0.24);
        icon.setEffect(glow);
        icon.getChildren().addAll(halo, arrow);
        return icon;
    }

    private static Path createWavePath(double centerX, double topY, double bottomY, double width, double wave) {
        Path path = new Path();
        setWavePath(path, centerX, topY, bottomY, width, wave);
        return path;
    }

    private static void setWavePath(Path path, double centerX, double topY, double bottomY, double width, double wave) {
        double height = Math.max(1, bottomY - topY);
        double left = centerX - width / 2.0;
        double right = centerX + width / 2.0;
        double yOne = bottomY - height * 0.34;
        double yTwo = bottomY - height * 0.68;

        path.getElements().setAll(
                new MoveTo(left, bottomY),
                new CubicCurveTo(left - wave, yOne, left + wave, yTwo, left, topY),
                new LineTo(right, topY),
                new CubicCurveTo(right - wave, yTwo, right + wave, yOne, right, bottomY),
                new ClosePath()
        );
    }

    private static void addStreamRipples(Group group, double centerX, double holdHeight, Color laneColor) {
        double[] ratios = {0.24, 0.50, 0.74};

        for (int i = 0; i < ratios.length; i++) {
            double offsetX = i % 2 == 0 ? -11 : 10;
            Circle ripple = new Circle(centerX + offsetX, -holdHeight * ratios[i], 7 + i * 2);
            ripple.setFill(Color.TRANSPARENT);
            ripple.setStroke(withOpacity(laneColor, 0.36));
            ripple.setStrokeWidth(1.4);
            ripple.setUserData("ripple");
            ripple.getProperties().put(HOLD_RATIO_KEY, ratios[i]);
            ripple.getProperties().put(BASE_X_KEY, centerX + offsetX);
            group.getChildren().add(ripple);
        }
    }

    private static void updateStreamFill(Node node, double maxHeight, double progress, Color holdColor, double graceRatio) {
        if (!(node instanceof Path fill)) {
            return;
        }

        double filledHeight = Math.max(1, maxHeight * progress);
        double centerX = propertyDouble(fill, HOLD_CENTER_X_KEY, 0);
        double width = propertyDouble(fill, HOLD_WIDTH_KEY, 54);
        double clampedGrace = clamp(graceRatio, 0, 1);

        fill.setVisible(progress > 0.015);
        fill.setOpacity(0.35 + (0.65 * clampedGrace));
        fill.setFill(withOpacity(holdColor, 0.58));
        fill.setStroke(withOpacity(Color.WHITE, 0.36));
        setWavePath(fill, centerX, -filledHeight, 0, width, 5.5 + progress * 3.0);
    }

    private static void updateStreamBody(Node node, Color holdColor, double graceRatio) {
        if (!(node instanceof Path body)) {
            return;
        }

        double clampedGrace = clamp(graceRatio, 0, 1);
        body.setFill(withOpacity(holdColor, 0.08 + 0.08 * clampedGrace));
        body.setStroke(withOpacity(holdColor, 0.22 + 0.20 * clampedGrace));
    }

    private static void updateStreamRipple(Circle ripple, double maxHeight, double progress, Color holdColor, double graceRatio) {
        double ratio = propertyDouble(ripple, HOLD_RATIO_KEY, 0.5);
        double baseX = propertyDouble(ripple, BASE_X_KEY, ripple.getCenterX());
        double lit = progress >= ratio ? 1.0 : 0.0;
        double clampedGrace = clamp(graceRatio, 0, 1);

        ripple.setCenterX(baseX + Math.sin(progress * 10 + ratio * 6) * 2.4);
        ripple.setCenterY(-maxHeight * ratio);
        ripple.setStroke(withOpacity(holdColor, lit > 0 ? 0.42 * clampedGrace : 0.18));
        ripple.setOpacity(lit > 0 ? 1.0 : 0.55);
    }

    private static void updateArrowFill(Node node, Color fill) {
        if ("arrowShape".equals(node.getUserData()) && node instanceof Polygon arrow) {
            arrow.setFill(fill);
            return;
        }

        if (node instanceof Group group) {
            for (Node child : group.getChildren()) {
                updateArrowFill(child, fill);
            }
        }
    }

    private static void updateArrowStroke(Node node, Color stroke) {
        if ("arrowShape".equals(node.getUserData()) && node instanceof Polygon arrow) {
            arrow.setStroke(stroke);
            return;
        }

        if (node instanceof Group group) {
            for (Node child : group.getChildren()) {
                updateArrowStroke(child, stroke);
            }
        }
    }

    private static void updateArrowHalo(Node node, Color fill, boolean pressed) {
        if ("arrowHalo".equals(node.getUserData()) && node instanceof Circle halo) {
            halo.setFill(withOpacity(fill, pressed ? 0.34 : 0.08));
            return;
        }

        if (node instanceof Group group) {
            for (Node child : group.getChildren()) {
                updateArrowHalo(child, fill, pressed);
            }
        }
    }

    private static double holdHeight(Group noteGroup, Note note) {
        Object value = noteGroup.getProperties().get(HOLD_HEIGHT_KEY);

        if (value instanceof Double height) {
            return height;
        }

        return note.getHoldDuration() * 300;
    }

    private static JourneyTheme themeFor(Group noteGroup) {
        Object value = noteGroup.getProperties().get(THEME_KEY);

        if (value instanceof JourneyTheme theme) {
            return theme;
        }

        return JourneyTheme.waves();
    }

    private static Color holdColor(Note note, boolean keyHeld, JourneyTheme theme) {
        if (note.isHoldDropped()) {
            return Color.CRIMSON;
        }

        if (note.isReleaseGraceActive() || !keyHeld) {
            return Color.ORANGE;
        }

        return colorForLane(note.getLane(), theme);
    }

    public static Color colorForLane(int lane) {
        return colorForLane(lane, JourneyTheme.waves());
    }

    public static Color colorForLane(int lane, JourneyTheme theme) {
        return theme.laneColor(lane);
    }

    private static Color noteColor(Note note, JourneyTheme theme) {
        if (note.isGoldNote()) {
            return theme.getGoldNoteColor();
        }

        return colorForLane(note.getLane(), theme);
    }

    public static Color legacyColorForLane(int lane) {
        return switch (lane) {
            case 0 -> Color.rgb(255, 92, 122);
            case 1 -> Color.rgb(89, 198, 255);
            case 2 -> Color.rgb(120, 230, 150);
            case 3 -> Color.rgb(255, 202, 84);
            default -> Color.WHITE;
        };
    }

    public static String symbolForLane(int lane) {
        return switch (lane) {
            case 0 -> "\u2190";
            case 1 -> "\u2191";
            case 2 -> "\u2193";
            case 3 -> "\u2192";
            default -> "?";
        };
    }

    private static double rotationForLane(int lane) {
        return switch (lane) {
            case 0 -> -90;
            case 1 -> 0;
            case 2 -> 180;
            case 3 -> 90;
            default -> 0;
        };
    }

    private static double propertyDouble(Node node, String key, double fallback) {
        Object value = node.getProperties().get(key);

        if (value instanceof Double number) {
            return number;
        }

        return fallback;
    }

    private static Color withOpacity(Color color, double opacity) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), clamp(opacity, 0, 1));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
