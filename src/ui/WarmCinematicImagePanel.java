package ui;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Random;

public class WarmCinematicImagePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int GRAIN_POINTS = 650;

    private final ImageIcon imageAsset;
    private final int[] grainX = new int[GRAIN_POINTS];
    private final int[] grainY = new int[GRAIN_POINTS];
    private final int[] grainAlpha = new int[GRAIN_POINTS];

    public WarmCinematicImagePanel(ImageIcon imageAsset) {
        this.imageAsset = imageAsset;

        Random random = new Random(1427L);
        for (int i = 0; i < GRAIN_POINTS; i++) {
            grainX[i] = random.nextInt(1000);
            grainY[i] = random.nextInt(1000);
            grainAlpha[i] = 10 + random.nextInt(18);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (imageAsset == null) {
            g.setPaint(new GradientPaint(0, 0, new Color(10, 8, 7), 0, getHeight(), new Color(24, 14, 8)));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            Image image = imageAsset.getImage();
            int imageWidth = Math.max(1, imageAsset.getIconWidth());
            int imageHeight = Math.max(1, imageAsset.getIconHeight());
            double scale = Math.max(getWidth() / (double) imageWidth, getHeight() / (double) imageHeight);
            int drawWidth = (int) Math.ceil(imageWidth * scale);
            int drawHeight = (int) Math.ceil(imageHeight * scale);
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;

            g.drawImage(image, x, y, drawWidth, drawHeight, this);
        }

        paintWarmCinematicFilter(g);
        g.dispose();
    }

    private void paintWarmCinematicFilter(Graphics2D g) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        g.setColor(new Color(62, 32, 10, 86));
        g.fillRect(0, 0, width, height);

        g.setPaint(new GradientPaint(
                0,
                0,
                new Color(250, 210, 135, 34),
                0,
                Math.max(1, height / 2),
                new Color(250, 210, 135, 0)
        ));
        g.fillRect(0, 0, width, height);

        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(width * 0.34, height * 0.34),
                Math.max(width, height) * 0.58f,
                new float[]{0f, 0.58f, 1f},
                new Color[]{
                        new Color(230, 166, 72, 36),
                        new Color(95, 55, 22, 18),
                        new Color(0, 0, 0, 0)
                }
        ));
        g.fillRect(0, 0, width, height);

        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(width * 0.52, height * 0.45),
                Math.max(width, height) * 0.74f,
                new float[]{0f, 0.62f, 1f},
                new Color[]{
                        new Color(0, 0, 0, 0),
                        new Color(37, 21, 8, 40),
                        new Color(0, 0, 0, 152)
                }
        ));
        g.fillRect(0, 0, width, height);

        paintFilmGrain(g, width, height);
    }

    private void paintFilmGrain(Graphics2D g, int width, int height) {
        for (int i = 0; i < GRAIN_POINTS; i++) {
            int x = grainX[i] * width / 1000;
            int y = grainY[i] * height / 1000;
            int value = (i % 3 == 0) ? 255 : 35;
            g.setColor(new Color(value, value, value, grainAlpha[i]));
            g.fillRect(x, y, 1, 1);
        }
    }
}
