package screens;

import config.GameConfig;
import manager.SaveManager;
import manager.ScreenManager;
import model.Stages.PlayableStage;
import score.ScoreTracker;
import ui.GameUiFactory;
import ui.JourneyTheme;
import ui.NoteRenderer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class ResultsScreen {
    private static final String RESULT_FONT_FAMILY = "Georgia";
    private static final Color GOLD = new Color(220, 178, 82);
    private static final Color TEXT = new Color(242, 235, 221);
    private static final Color TEXT_MUTED = new Color(177, 166, 145);
    private static final Color PANEL_FILL = new Color(10, 9, 9, 172);
    private static final Color PANEL_BORDER = new Color(128, 99, 48, 120);

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

        JPanel root = new ResultsBackgroundPanel();
        root.setLayout(null);

        JPanel header = createHeader(cleared);
        header.setBounds(74, 42, 852, 96);
        root.add(header);

        GradeEmblemPanel gradeEmblem = new GradeEmblemPanel(scoreTracker.getGrade(), gradeColor(), cleared);
        gradeEmblem.setBounds(82, 156, 328, 260);
        root.add(gradeEmblem);

        JPanel statsPanel = createStatsPanel(newBest, previousBest, unlockedNext, cleared);
        statsPanel.setBounds(444, 158, 470, 250);
        root.add(statsPanel);

        JPanel metricBoxes = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        metricBoxes.setOpaque(false);
        metricBoxes.setBounds(244, 426, 512, 76);
        metricBoxes.add(createMetricBox("ACCURACY", String.format("%.1f%%", scoreTracker.getAccuracy())));
        metricBoxes.add(createMetricBox("RANK", scoreTracker.getGrade()));
        root.add(metricBoxes);

        JPanel buttons = createButtonRow(nextStage);
        buttons.setBounds(244, 526, 512, 42);
        root.add(buttons);

        return root;
    }

    private JPanel createHeader(boolean cleared) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel sceneLabel = label(
                "SCENE " + String.format("%02d", playableStage.getNumber()),
                GOLD,
                Font.BOLD,
                15
        );
        JLabel title = label(playableStage.getTitle(), TEXT, Font.BOLD, 42);
        JLabel subtitle = label(
                cleared ? "Stage cleared" : "Stage failed",
                cleared ? TEXT_MUTED : new Color(238, 126, 104),
                Font.PLAIN,
                18
        );

        addLeft(panel, sceneLabel);
        panel.add(Box.createVerticalStrut(5));
        addLeft(panel, title);
        panel.add(Box.createVerticalStrut(5));
        addLeft(panel, subtitle);
        return panel;
    }

    private JPanel createStatsPanel(boolean newBest, int previousBest, boolean unlockedNext, boolean cleared) {
        JPanel panel = new TranslucentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel heading = label("PERFORMANCE", GOLD, Font.BOLD, 16);
        addLeft(panel, heading);
        panel.add(Box.createVerticalStrut(18));
        panel.add(statRow("Score", Integer.toString(scoreTracker.getScore()), TEXT));
        panel.add(Box.createVerticalStrut(14));
        panel.add(statRow(
                newBest ? "Best" : "Previous Best",
                newBest ? "New Best" : Integer.toString(previousBest),
                newBest ? theme.getGoldNoteColor() : TEXT
        ));
        panel.add(Box.createVerticalStrut(14));
        panel.add(statRow("Max Combo", Integer.toString(scoreTracker.getMaxCombo()), TEXT));
        panel.add(Box.createVerticalStrut(14));
        panel.add(statRow("Result", resultMessage(cleared, unlockedNext), cleared ? TEXT : new Color(238, 126, 104)));

        return panel;
    }

    private JPanel statRow(String labelText, String valueText, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(414, 30));

        JLabel label = label(labelText, TEXT_MUTED, Font.PLAIN, 17);
        JLabel value = label(valueText, valueColor, Font.BOLD, 19);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JPanel createMetricBox(String labelText, String valueText) {
        JPanel box = new TranslucentPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        box.setPreferredSize(new Dimension(200, 68));

        JLabel label = label(labelText, TEXT_MUTED, Font.BOLD, 13);
        JLabel value = label(valueText, GOLD, Font.BOLD, 24);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        value.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(label);
        box.add(Box.createVerticalStrut(4));
        box.add(value);
        return box;
    }

    private JPanel createButtonRow(PlayableStage nextStage) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        buttons.setOpaque(false);

        JButton retryButton = GameUiFactory.createSmallButton("RETRY");
        JButton continueButton = GameUiFactory.createSmallButton("CONTINUE");
        JButton selectButton = GameUiFactory.createSmallButton("SONG SELECT");
        setButtonSize(retryButton, 118);
        setButtonSize(continueButton, 132);
        setButtonSize(selectButton, 154);

        retryButton.addActionListener(e -> controller.startStage(playableStage));
        selectButton.addActionListener(e -> controller.showJourneySelect(playableStage.getJourneyId()));

        if (nextStage != null && saveManager.isUnlocked(nextStage)) {
            continueButton.addActionListener(e -> controller.showLore(nextStage));
        } else {
            continueButton.setEnabled(false);
            continueButton.setForeground(new Color(102, 95, 82));
            continueButton.setCursor(java.awt.Cursor.getDefaultCursor());
        }

        buttons.add(retryButton);
        buttons.add(continueButton);
        buttons.add(selectButton);
        return buttons;
    }

    private void setButtonSize(JButton button, int width) {
        Dimension size = new Dimension(width, 40);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
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

    private Color gradeColor() {
        return switch (scoreTracker.getGrade()) {
            case "S" -> new Color(255, 215, 0);
            case "A" -> new Color(165, 228, 160);
            case "B" -> new Color(121, 202, 232);
            case "C" -> TEXT;
            case "D" -> new Color(235, 166, 92);
            default -> new Color(232, 86, 74);
        };
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

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(
                    0,
                    0,
                    new Color(16, 13, 11),
                    0,
                    getHeight(),
                    new Color(5, 5, 6)
            ));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(theme.getAccentColor().getRed(), theme.getAccentColor().getGreen(), theme.getAccentColor().getBlue(), 18));
            g.fill(new Ellipse2D.Double(80, 90, 390, 390));
            g.setColor(new Color(0, 0, 0, 108));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    private static final class TranslucentPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private TranslucentPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(PANEL_FILL);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            g.setColor(PANEL_BORDER);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, 20, 20));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class GradeEmblemPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final String grade;
        private final Color gradeColor;
        private final boolean cleared;

        private GradeEmblemPanel(String grade, Color gradeColor, boolean cleared) {
            this.grade = grade;
            this.gradeColor = gradeColor;
            this.cleared = cleared;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double centerX = getWidth() / 2.0;
            double centerY = 112;
            double outer = 184;
            double inner = 142;

            g.setColor(NoteRenderer.withOpacity(gradeColor, 0.20));
            g.fill(new Ellipse2D.Double(centerX - outer / 2, centerY - outer / 2, outer, outer));
            g.setColor(PANEL_FILL);
            g.fill(new Ellipse2D.Double(centerX - inner / 2, centerY - inner / 2, inner, inner));
            g.setColor(NoteRenderer.withOpacity(gradeColor, 0.82));
            g.setStroke(new BasicStroke(3.4f));
            g.draw(new Ellipse2D.Double(centerX - inner / 2, centerY - inner / 2, inner, inner));

            g.setFont(new Font(RESULT_FONT_FAMILY, Font.BOLD, 86));
            g.setColor(gradeColor);
            String gradeText = grade;
            int gradeX = (getWidth() - g.getFontMetrics().stringWidth(gradeText)) / 2;
            g.drawString(gradeText, gradeX, 142);

            g.setFont(new Font(RESULT_FONT_FAMILY, Font.BOLD, 18));
            g.setColor(cleared ? TEXT : new Color(238, 126, 104));
            String result = cleared ? "CLEARED" : "FAILED";
            int resultX = (getWidth() - g.getFontMetrics().stringWidth(result)) / 2;
            g.drawString(result, resultX, 226);

            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
