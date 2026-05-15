package screens;

import content.StageResultStoryCatalog;
import manager.ScreenManager;
import model.Stages.PlayableStage;
import score.ScoreTracker;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

public class StageResultStoryScreen {
    private static final String STORY_FONT_FAMILY = "Georgia";
    private static final Color BLUE_TOP = new Color(9, 28, 48);
    private static final Color BLUE_BOTTOM = new Color(2, 7, 14);
    private static final Color PANEL_FILL = new Color(5, 14, 25, 182);
    private static final Color PANEL_BORDER = new Color(119, 169, 205, 95);
    private static final Color GOLD = new Color(224, 184, 96);
    private static final Color TEXT = new Color(236, 244, 247);
    private static final Color TEXT_MUTED = new Color(164, 197, 213);

    private final ScreenManager controller;
    private final PlayableStage playableStage;
    private final ScoreTracker scoreTracker;

    public StageResultStoryScreen(
            ScreenManager controller,
            PlayableStage playableStage,
            ScoreTracker scoreTracker
    ) {
        this.controller = controller;
        this.playableStage = playableStage;
        this.scoreTracker = scoreTracker;
    }

    public JPanel create() {
        JPanel root = new StoryBackgroundPanel();
        root.setLayout(null);
        root.setFocusable(true);

        JLabel title = centeredLabel(playableStage.getTitle().toUpperCase(), GOLD, Font.BOLD, 56);
        title.setBounds(80, 44, 840, 66);
        root.add(title);

        JLabel subtitle = centeredLabel(
                StageResultStoryCatalog.titleFor(playableStage, scoreTracker),
                TEXT_MUTED,
                Font.PLAIN,
                19
        );
        subtitle.setBounds(160, 108, 680, 30);
        root.add(subtitle);

        JPanel narrativePanel = new NarrativePanel();
        narrativePanel.setLayout(null);
        narrativePanel.setBounds(210, 166, 580, 286);

        JTextPane story = centeredTextPane(StageResultStoryCatalog.storyFor(playableStage, scoreTracker));
        story.setBounds(66, 88, 448, 126);
        narrativePanel.add(story);
        root.add(narrativePanel);

        JLabel prompt = centeredLabel("press space to continue", TEXT_MUTED, Font.BOLD, 15);
        prompt.setBounds(0, 522, 1000, 30);
        root.add(prompt);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "showResults");
        root.getActionMap().put("showResults", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.showResults(playableStage, scoreTracker);
            }
        });

        return root;
    }

    private JLabel centeredLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setForeground(color);
        label.setFont(new Font(STORY_FONT_FAMILY, style, size));
        return label;
    }

    private JTextPane centeredTextPane(String text) {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setOpaque(false);
        textPane.setForeground(TEXT);
        textPane.setFont(new Font(STORY_FONT_FAMILY, Font.PLAIN, storyFontSize(text)));
        textPane.setBorder(BorderFactory.createEmptyBorder());
        textPane.setText(text);

        StyledDocument document = textPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attributes, 0.06f);
        document.setParagraphAttributes(0, document.getLength(), attributes, false);
        textPane.setCaretPosition(0);
        return textPane;
    }

    private int storyFontSize(String text) {
        if (text.length() > 560) {
            return 13;
        }

        if (text.length() > 420) {
            return 15;
        }

        if (text.length() > 280) {
            return 17;
        }

        return 20;
    }

    private static final class StoryBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setPaint(new GradientPaint(0, 0, BLUE_TOP, 0, getHeight(), BLUE_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(60, 132, 178, 24));
            g.fill(new Ellipse2D.Double(245, 95, 510, 410));
            g.setColor(new Color(0, 0, 0, 92));
            g.fillRect(0, 0, getWidth(), getHeight());

            drawSideDecorations(g);
            drawVignette(g);
            g.dispose();
        }

        private void drawSideDecorations(Graphics2D g) {
            g.setStroke(new BasicStroke(1.3f));
            g.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 92));

            drawOrnament(g, 126, 238);
            drawOrnament(g, 874, 238);
        }

        private void drawOrnament(Graphics2D g, int centerX, int topY) {
            g.draw(new Line2D.Double(centerX, topY, centerX, topY + 126));
            g.draw(new Ellipse2D.Double(centerX - 8, topY - 10, 16, 16));
            g.draw(new Ellipse2D.Double(centerX - 8, topY + 121, 16, 16));
            g.draw(new Line2D.Double(centerX - 42, topY + 63, centerX - 14, topY + 63));
            g.draw(new Line2D.Double(centerX + 14, topY + 63, centerX + 42, topY + 63));
        }

        private void drawVignette(Graphics2D g) {
            g.setColor(new Color(0, 0, 0, 54));
            g.fillRect(0, 0, getWidth(), 70);
            g.fillRect(0, getHeight() - 78, getWidth(), 78);
            g.fillRect(0, 0, 70, getHeight());
            g.fillRect(getWidth() - 70, 0, 70, getHeight());
        }
    }

    private static final class NarrativePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private NarrativePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(PANEL_FILL);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 24, 24));
            g.setColor(PANEL_BORDER);
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, 24, 24));

            drawDivider(g, 54);
            drawDivider(g, getHeight() - 54);

            g.dispose();
            super.paintComponent(graphics);
        }

        private void drawDivider(Graphics2D g, int y) {
            int centerX = getWidth() / 2;
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 120));
            g.draw(new Line2D.Double(94, y, centerX - 20, y));
            g.draw(new Line2D.Double(centerX + 20, y, getWidth() - 94, y));
            g.fill(new Ellipse2D.Double(centerX - 4, y - 4, 8, 8));
        }
    }
}
