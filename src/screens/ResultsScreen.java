package screens;

import config.AssetCatalog;
import config.GameConfig;
import manager.SaveManager;
import manager.ScreenManager;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import ui.AnimatedGifBackground;
import ui.GameUiFactory;
import ui.JourneyTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Locale;

public class ResultsScreen {
    private static final String RESULT_FONT_FAMILY = "Georgia";
    private static final Color ROOT_TOP = new Color(48, 31, 20);
    private static final Color ROOT_BOTTOM = new Color(12, 8, 6);
    private static final Color GOLD = new Color(154, 100, 38);
    private static final Color TEXT = new Color(69, 43, 24);
    private static final Color TEXT_MUTED = new Color(116, 82, 48);
    private static final Color PARCHMENT = new Color(230, 207, 166);
    private static final Color PARCHMENT_DARK = new Color(202, 165, 112);
    private static final Color PANEL_BORDER = new Color(122, 74, 35, 96);
    private static final float BACKGROUND_IMAGE_OPACITY = 0.35f;
    private static final int RESULT_PANEL_WIDTH = 382;
    private static final int RESULT_PANEL_HEIGHT = 326;
    private static final int RESULT_BUTTON_WIDTH = 195;

    private final ScreenManager controller;
    private final SaveManager saveManager;
    private final PlayableStage playableStage;
    private final ScoreTracker scoreTracker;
    private final JourneyTheme theme;

    public ResultsScreen(
            ScreenManager controller,
            SaveManager saveManager,
            PlayableStage playableStage,
            ScoreTracker scoreTracker
    ) {
        this.controller = controller;
        this.saveManager = saveManager;
        this.playableStage = playableStage;
        this.scoreTracker = scoreTracker;
        this.theme = JourneyTheme.forStage(playableStage);
    }

    public JPanel create() {
        boolean cleared = scoreTracker.getAccuracy() >= GameConfig.CLEAR_ACCURACY;
        int previousBest = saveManager.getHighScore(playableStage);
        boolean newBest = scoreTracker.getScore() > previousBest;
        boolean unlockedNext = saveManager.recordResult(playableStage, scoreTracker);
        PlayableStage nextStage = playableStage.next();

        JPanel root = new ResultsBackgroundPanel(AnimatedGifBackground.load(AssetCatalog.titleScreenUrl()));
        root.setLayout(null);

        JPanel header = createHeader(cleared);
        header.setBounds(124, 42, 752, 112);
        root.add(header);

        GradeEmblemPanel gradeEmblem = new GradeEmblemPanel(scoreTracker.getGrade());
        gradeEmblem.setBounds(96, 158, RESULT_PANEL_WIDTH, RESULT_PANEL_HEIGHT);
        root.add(gradeEmblem);

        JPanel statsPanel = createStatsPanel(newBest, previousBest, unlockedNext, cleared);
        statsPanel.setBounds(522, 158, RESULT_PANEL_WIDTH, RESULT_PANEL_HEIGHT);
        root.add(statsPanel);

        JPanel buttons = createButtonRow(nextStage, cleared);
        buttons.setBounds(140, 506, 720, 84);
        root.add(buttons);

        return root;
    }

    private JPanel createHeader(boolean cleared) {
        JPanel panel = new ResultPlaceholderPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 24, 12, 24));

        JLabel title = label(playableStage.getTitle().toUpperCase(Locale.ROOT), TEXT, Font.BOLD, 42);
        JLabel subtitle = label(
                cleared ? "SCENE CLEARED" : "SCENE FAILED",
                cleared ? TEXT_MUTED : new Color(238, 126, 104),
                Font.PLAIN,
                18
        );
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        return panel;
    }

    private JPanel createStatsPanel(boolean newBest, int previousBest, boolean unlockedNext, boolean cleared) {
        JPanel panel = new ResultPlaceholderPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel heading = label("PERFORMANCE", GOLD, Font.BOLD, 16);
        addLeft(panel, heading);
        panel.add(Box.createVerticalStrut(18));
        panel.add(statRow("Score", Integer.toString(scoreTracker.getScore()), TEXT));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statRow(
                newBest ? "Best" : "Previous Best",
                newBest ? "New Best" : Integer.toString(previousBest),
                newBest ? theme.getGoldNoteColor() : TEXT
        ));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statRow("Max Combo", Integer.toString(scoreTracker.getMaxCombo()), TEXT));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statRow("Accuracy", String.format("%.1f%%", scoreTracker.getAccuracy()), TEXT));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statRow("Rank", scoreTracker.getGrade(), GOLD));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statRow("Result", resultMessage(cleared, unlockedNext), cleared ? TEXT : new Color(238, 126, 104)));

        return panel;
    }

    private JPanel statRow(String labelText, String valueText, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(390, 30));
        row.setMaximumSize(new Dimension(390, 30));

        JLabel label = label(labelText, TEXT_MUTED, Font.PLAIN, 17);
        JLabel value = label(valueText, valueColor, Font.BOLD, 19);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        value.setPreferredSize(new Dimension(210, 30));

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JPanel createButtonRow(PlayableStage nextStage, boolean cleared) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        buttons.setOpaque(false);

        JButton retryButton = GameUiFactory.createImageStateButton(
                AssetCatalog.buttonStateUrl("Rtry", "S"),
                AssetCatalog.buttonStateUrl("Rtry", "H"),
                AssetCatalog.buttonStateUrl("Rtry", "P"),
                "RETRY",
                RESULT_BUTTON_WIDTH
        );
        JButton scenesButton = GameUiFactory.createImageStateButton(
                AssetCatalog.buttonStateUrl("Home", "S"),
                AssetCatalog.buttonStateUrl("Home", "H"),
                AssetCatalog.buttonStateUrl("Home", "P"),
                "HOME",
                RESULT_BUTTON_WIDTH
        );

        retryButton.addActionListener(e -> controller.startStage(playableStage));
        scenesButton.addActionListener(e -> controller.showJourneySelect(playableStage.getJourneyId()));

        buttons.add(retryButton);
        if (cleared && nextStage != null && saveManager.isUnlocked(nextStage)) {
            JButton nextButton = GameUiFactory.createImageStateButton(
                    AssetCatalog.buttonStateUrl("Next", "S"),
                    AssetCatalog.buttonStateUrl("Next", "H"),
                    AssetCatalog.buttonStateUrl("Next", "P"),
                    "NEXT",
                    RESULT_BUTTON_WIDTH
            );
            nextButton.addActionListener(e -> controller.showLore(nextStage));
            buttons.add(nextButton);
        }
        buttons.add(scenesButton);
        return buttons;
    }

    private String resultMessage(boolean cleared, boolean unlockedNext) {
        if (unlockedNext) {
            return "Next scene unlocked";
        }

        if (cleared) {
            return "Scene cleared";
        }

        return String.format("Reach %.0f%% accuracy", GameConfig.CLEAR_ACCURACY);
    }

    private JLabel label(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(RESULT_FONT_FAMILY, style, size));
        return label;
    }

    private void addLeft(JPanel panel, javax.swing.JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(component);
    }

    private final class ResultsBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final AnimatedGifBackground backgroundImage;
        private final javax.swing.Timer animationTimer;

        private ResultsBackgroundPanel(AnimatedGifBackground backgroundImage) {
            this.backgroundImage = backgroundImage;
            setOpaque(true);

            if (backgroundImage != null && backgroundImage.isAnimated()) {
                animationTimer = new javax.swing.Timer(33, e -> repaint());
                animationTimer.start();
            } else {
                animationTimer = null;
            }
        }

        @Override
        public void removeNotify() {
            if (animationTimer != null) {
                animationTimer.stop();
            }

            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setPaint(new GradientPaint(
                    0,
                    0,
                    ROOT_TOP,
                    0,
                    getHeight(),
                    ROOT_BOTTOM
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                g.setComposite(AlphaComposite.SrcOver.derive(BACKGROUND_IMAGE_OPACITY));
                drawCoverImage(g, frame);
                g.setComposite(AlphaComposite.SrcOver);
                g.setColor(new Color(0, 0, 0, 124));
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            g.dispose();
        }

        private void drawCoverImage(Graphics2D g, BufferedImage frame) {
            int imageWidth = frame.getWidth();
            int imageHeight = frame.getHeight();
            double scale = Math.max(getWidth() / (double) imageWidth, getHeight() / (double) imageHeight);
            int drawWidth = (int) Math.ceil(imageWidth * scale);
            int drawHeight = (int) Math.ceil(imageHeight * scale);
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;

            g.drawImage(frame, x, y, drawWidth, drawHeight, this);
        }
    }

    private static class ResultPlaceholderPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final boolean drawOrnaments;

        private ResultPlaceholderPanel() {
            this(true);
        }

        private ResultPlaceholderPanel(boolean drawOrnaments) {
            this.drawOrnaments = drawOrnaments;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(0, 0, 0, 72));
            g.fill(new RoundRectangle2D.Double(6, 8, getWidth() - 12, getHeight() - 10, 18, 18));

            g.setPaint(new GradientPaint(0, 0, PARCHMENT, 0, getHeight(), PARCHMENT_DARK));
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 6, getHeight() - 8, 18, 18));

            g.setColor(PANEL_BORDER);
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new RoundRectangle2D.Double(5, 5, getWidth() - 18, getHeight() - 20, 14, 14));

            if (drawOrnaments) {
                g.setColor(new Color(102, 66, 34, 28));
                g.setStroke(new BasicStroke(1f));
                for (int i = 0; i < 3; i++) {
                    int radius = 42 + i * 18;
                    g.drawOval(getWidth() - 120 - i * 6, 24 - i * 6, radius, radius);
                }
            }

            g.dispose();
        }
    }

    private static final class GradeEmblemPanel extends ResultPlaceholderPanel {
        private static final long serialVersionUID = 1L;

        private final String grade;
        private final BufferedImage rankImage;

        private GradeEmblemPanel(String grade) {
            super(false);
            this.grade = grade;
            this.rankImage = loadRankImage(grade);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (rankImage != null) {
                drawRankImage(g);
            } else {
                g.setFont(new Font(RESULT_FONT_FAMILY, Font.BOLD, 86));
                g.setColor(GOLD);
                String gradeText = grade;
                int gradeX = (getWidth() - g.getFontMetrics().stringWidth(gradeText)) / 2;
                g.drawString(gradeText, gradeX, 142);
            }

            g.dispose();
        }

        private void drawRankImage(Graphics2D g) {
            java.awt.Rectangle bounds = imageContentBounds(rankImage);
            double scale = Math.min(
                    (getWidth() - 72) / (double) bounds.width,
                    (getHeight() - 58) / (double) bounds.height
            );
            int drawWidth = (int) Math.round(bounds.width * scale);
            int drawHeight = (int) Math.round(bounds.height * scale);
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;

            g.drawImage(
                    rankImage,
                    x,
                    y,
                    x + drawWidth,
                    y + drawHeight,
                    bounds.x,
                    bounds.y,
                    bounds.x + bounds.width,
                    bounds.y + bounds.height,
                    this
            );
        }

        private static java.awt.Rectangle imageContentBounds(BufferedImage image) {
            int minX = image.getWidth();
            int minY = image.getHeight();
            int maxX = -1;
            int maxY = -1;

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) > 0) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }

            if (maxX < minX || maxY < minY) {
                return new java.awt.Rectangle(0, 0, image.getWidth(), image.getHeight());
            }

            return new java.awt.Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }

        private static BufferedImage loadRankImage(String grade) {
            URL rankUrl = AssetCatalog.rankUrl(grade);
            if (rankUrl == null) {
                return null;
            }

            try {
                return ImageIO.read(rankUrl);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
