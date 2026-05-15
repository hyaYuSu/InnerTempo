package ui;

import model.Note;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public final class NoteRenderer {
    private static final double ARROW_CENTER_OFFSET = 52;

    private NoteRenderer() {
    }

    public static void drawNote(
            Graphics2D g,
            double x,
            double y,
            Note note,
            double pixelsPerSecond,
            JourneyTheme theme,
            boolean keyHeld
    ) {
        if (note.isHoldNote()) {
            drawHoldStream(g, x + ARROW_CENTER_OFFSET, y, note, pixelsPerSecond, theme, keyHeld);
        }

        drawArrowIcon(
                g,
                note.getLane(),
                x + ARROW_CENTER_OFFSET,
                y,
                note.isGoldNote() ? 88 : 80,
                noteColor(note, theme),
                note.isGoldNote() ? Color.WHITE : withOpacity(Color.WHITE, 0.78),
                note.isGoldNote(),
                note.isGoldNote() ? 0.32 : 0.18
        );
    }

    public static void drawReceptor(
            Graphics2D g,
            double centerX,
            double centerY,
            int lane,
            JourneyTheme theme,
            boolean pressed
    ) {
        Color fill = pressed ? theme.laneColor(lane) : Color.WHITE;
        Color stroke = pressed ? Color.WHITE : theme.laneColor(lane);
        drawArrowIcon(g, lane, centerX, centerY, pressed ? 82 : 74, fill, stroke, pressed, pressed ? 0.34 : 0.08);
    }

    private static void drawHoldStream(
            Graphics2D g,
            double centerX,
            double y,
            Note note,
            double pixelsPerSecond,
            JourneyTheme theme,
            boolean keyHeld
    ) {
        double holdHeight = note.getHoldDuration() * pixelsPerSecond;
        double progress = Math.min(note.getHoldProgress() / note.getHoldDuration(), 1.0);
        Color holdColor = holdColor(note, keyHeld, theme);
        double graceRatio = note.isReleaseGraceActive() ? 0.55 : 1.0;

        Path2D body = createWavePath(centerX, y - holdHeight, y, 68, 8);
        g.setColor(withOpacity(holdColor, 0.08 + 0.08 * graceRatio));
        g.fill(body);
        g.setColor(withOpacity(holdColor, 0.22 + 0.20 * graceRatio));
        g.setStroke(new BasicStroke(2f));
        g.draw(body);

        Shape tail = new java.awt.geom.RoundRectangle2D.Double(centerX - 36, y - holdHeight - 5, 72, 10, 12, 12);
        g.setColor(withOpacity(holdColor, note.isHolding() ? 0.82 : 0.64));
        g.fill(tail);

        if (progress > 0.015) {
            double filledHeight = Math.max(1, holdHeight * progress);
            Path2D fill = createWavePath(centerX, y - filledHeight, y, 54, 5.5 + progress * 3.0);
            g.setColor(withOpacity(holdColor, 0.58));
            g.fill(fill);
            g.setColor(withOpacity(Color.WHITE, 0.36));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(fill);
        }

        double[] ratios = {0.24, 0.50, 0.74};
        for (int i = 0; i < ratios.length; i++) {
            double ratio = ratios[i];
            double offsetX = i % 2 == 0 ? -11 : 10;
            double rippleX = centerX + offsetX + Math.sin(progress * 10 + ratio * 6) * 2.4;
            double rippleY = y - holdHeight * ratio;
            double radius = 7 + i * 2;
            boolean lit = progress >= ratio;

            g.setColor(withOpacity(holdColor, lit ? 0.42 * graceRatio : 0.18));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Ellipse2D.Double(rippleX - radius, rippleY - radius, radius * 2, radius * 2));
        }
    }

    private static void drawArrowIcon(
            Graphics2D g,
            int lane,
            double centerX,
            double centerY,
            double size,
            Color fill,
            Color stroke,
            boolean strongGlow,
            double haloOpacity
    ) {
        double half = size / 2.0;

        g.setColor(withOpacity(fill, haloOpacity));
        g.fill(new Ellipse2D.Double(centerX - half * 0.78, centerY - half * 0.78, half * 1.56, half * 1.56));

        if (strongGlow) {
            g.setColor(withOpacity(fill, 0.16));
            g.fill(new Ellipse2D.Double(centerX - half * 1.05, centerY - half * 1.05, half * 2.10, half * 2.10));
        }

        Path2D arrow = new Path2D.Double();
        arrow.moveTo(centerX, centerY - half);
        arrow.lineTo(centerX + half * 0.58, centerY - half * 0.12);
        arrow.lineTo(centerX + half * 0.24, centerY - half * 0.12);
        arrow.lineTo(centerX + half * 0.24, centerY + half);
        arrow.lineTo(centerX - half * 0.24, centerY + half);
        arrow.lineTo(centerX - half * 0.24, centerY - half * 0.12);
        arrow.lineTo(centerX - half * 0.58, centerY - half * 0.12);
        arrow.closePath();

        AffineTransform rotate = AffineTransform.getRotateInstance(
                Math.toRadians(rotationForLane(lane)),
                centerX,
                centerY
        );
        Shape rotatedArrow = rotate.createTransformedShape(arrow);

        g.setColor(fill);
        g.fill(rotatedArrow);
        g.setColor(stroke);
        g.setStroke(new BasicStroke(strongGlow ? 5f : 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(rotatedArrow);
    }

    private static Path2D createWavePath(double centerX, double topY, double bottomY, double width, double wave) {
        double height = Math.max(1, bottomY - topY);
        double left = centerX - width / 2.0;
        double right = centerX + width / 2.0;
        double yOne = bottomY - height * 0.34;
        double yTwo = bottomY - height * 0.68;

        Path2D path = new Path2D.Double();
        path.moveTo(left, bottomY);
        path.curveTo(left - wave, yOne, left + wave, yTwo, left, topY);
        path.lineTo(right, topY);
        path.curveTo(right - wave, yTwo, right + wave, yOne, right, bottomY);
        path.closePath();
        return path;
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
            case 0 -> new Color(255, 92, 122);
            case 1 -> new Color(89, 198, 255);
            case 2 -> new Color(120, 230, 150);
            case 3 -> new Color(255, 202, 84);
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

    public static Color withOpacity(Color color, double opacity) {
        int alpha = (int) Math.round(clamp(opacity, 0, 1) * 255);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static Color holdColor(Note note, boolean keyHeld, JourneyTheme theme) {
        if (note.isHoldDropped()) {
            return new Color(220, 20, 60);
        }

        if (note.isReleaseGraceActive() || (note.isHolding() && !keyHeld)) {
            return Color.ORANGE;
        }

        return colorForLane(note.getLane(), theme);
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

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
