package ui;

import config.GameConfig;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class CenteredTypingStoryPanel extends JPanel {
    private static final int TYPE_INTERVAL_MILLIS = 25;
    private static final int TYPE_CHARS_PER_TICK = 1;
    private static final Font STORY_FONT = new Font("Trebuchet MS", Font.PLAIN, 24);
    private static final Font PROMPT_FONT = new Font("Trebuchet MS", Font.BOLD, 14);
    private static final Color STORY_TEXT = Color.WHITE;
    private static final Color PROMPT_TEXT = new Color(205, 213, 218);

    private final Color topColor;
    private final Color bottomColor;
    private final AnimatedGifBackground backgroundImage;
    private final List<String> pages;
    private final Runnable onComplete;
    private final JTextPane storyText;
    private final JLabel prompt;
    private final JButton nextButton;
    private final Timer animationTimer;
    private Timer typingTimer;
    private int pageIndex;
    private int visibleCharacters;
    private String fullPageText = "";

    public CenteredTypingStoryPanel(
            AnimatedGifBackground backgroundImage,
            Color topColor,
            Color bottomColor,
            List<String> pages,
            Runnable onComplete
    ) {
        this.backgroundImage = backgroundImage;
        this.topColor = topColor;
        this.bottomColor = bottomColor;
        this.pages = pages.isEmpty() ? List.of("") : pages;
        this.onComplete = onComplete;
        this.storyText = createStoryText();
        this.prompt = createPrompt();
        this.nextButton = createNextButton();
        this.animationTimer = backgroundImage != null && backgroundImage.isAnimated()
                ? new Timer(33, e -> repaint())
                : null;

        setOpaque(true);
        setFocusable(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 110, 0, 110));

        add(Box.createVerticalGlue());
        add(storyText);
        add(Box.createVerticalStrut(18));
        add(prompt);
        add(Box.createVerticalStrut(10));
        add(nextButton);
        add(Box.createVerticalGlue());

        installKeyBinding();
        startPage(0);

        if (animationTimer != null) {
            animationTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        stopTyping();

        if (animationTimer != null) {
            animationTimer.stop();
        }

        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
        g.fillRect(0, 0, getWidth(), getHeight());

        BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
        if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
            drawCoverImage(g, frame);
            g.setColor(new Color(2, 8, 18, 128));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 40), 0, getHeight(), new Color(0, 0, 0, 178)));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            drawOceanSilhouette(g);
        }

        g.dispose();
    }

    private JTextPane createStoryText() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setOpaque(false);
        textPane.setForeground(STORY_TEXT);
        textPane.setFont(STORY_FONT);
        textPane.setBorder(BorderFactory.createEmptyBorder());
        textPane.setPreferredSize(new Dimension(760, 230));
        textPane.setMaximumSize(new Dimension(760, 230));
        return textPane;
    }

    private JLabel createPrompt() {
        JLabel label = new JLabel("", JLabel.CENTER);
        label.setForeground(PROMPT_TEXT);
        label.setFont(PROMPT_FONT);
        label.setAlignmentX(CENTER_ALIGNMENT);
        return label;
    }

    private JButton createNextButton() {
        JButton button = GameUiFactory.createSmallButton("NEXT");
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.addActionListener(e -> advance());
        return button;
    }

    private void installKeyBinding() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "advanceStoryText");
        getActionMap().put("advanceStoryText", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                advance();
            }
        });
    }

    private void startPage(int nextPageIndex) {
        stopTyping();
        pageIndex = nextPageIndex;
        fullPageText = pages.get(pageIndex);
        visibleCharacters = 0;
        setStoryText("");
        updatePrompt();

        typingTimer = new Timer(TYPE_INTERVAL_MILLIS, e -> revealNextChunk());
        typingTimer.start();
    }

    private void revealNextChunk() {
        visibleCharacters = Math.min(fullPageText.length(), visibleCharacters + TYPE_CHARS_PER_TICK);
        setStoryText(fullPageText.substring(0, visibleCharacters));

        if (visibleCharacters >= fullPageText.length()) {
            stopTyping();
        }
    }

    private void advance() {
        if (visibleCharacters < fullPageText.length()) {
            stopTyping();
            visibleCharacters = fullPageText.length();
            setStoryText(fullPageText);
            return;
        }

        if (pageIndex >= pages.size() - 1) {
            stopTyping();
            onComplete.run();
            return;
        }

        startPage(pageIndex + 1);
    }

    private void setStoryText(String text) {
        storyText.setText(text);
        StyledDocument document = storyText.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attributes, 0.12f);
        document.setParagraphAttributes(0, document.getLength(), attributes, false);
        storyText.setCaretPosition(0);
        storyText.revalidate();
        storyText.repaint();
    }

    private void updatePrompt() {
        prompt.setText("press space or next  " + (pageIndex + 1) + "/" + pages.size());
    }

    private void stopTyping() {
        if (typingTimer != null) {
            typingTimer.stop();
            typingTimer = null;
        }
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

    private void drawOceanSilhouette(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = GameConfig.SCENE_WIDTH;
        int height = GameConfig.SCENE_HEIGHT;
        int horizon = 282;

        g.setPaint(new GradientPaint(0, 0, new Color(9, 32, 55, 92), 0, height, new Color(4, 9, 18, 154)));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(170, 210, 230, 34));
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 6; i++) {
            int y = horizon + i * 34;
            g.draw(new Line2D.Double(90, y, width - 90, y + (i % 2 == 0 ? 8 : -6)));
        }

        int boatX = width / 2 - 95;
        int boatY = horizon + 88;
        g.setColor(new Color(9, 7, 6, 190));
        g.fill(new RoundRectangle2D.Double(boatX, boatY, 190, 34, 36, 36));
        g.setColor(new Color(210, 176, 112, 90));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Line2D.Double(boatX + 24, boatY + 12, boatX - 68, boatY - 26));
        g.draw(new Line2D.Double(boatX + 166, boatY + 12, boatX + 258, boatY - 26));

        g.setColor(new Color(236, 238, 222, 80));
        g.fill(new Ellipse2D.Double(width / 2.0 - 5, boatY - 24, 10, 10));
    }
}
