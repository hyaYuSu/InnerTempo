package screens;

import content.StageResultStoryCatalog;
import config.AssetCatalog;
import manager.ScreenManager;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;
import score.ScoreTracker;
import ui.AnimatedGifBackground;
import ui.CenteredTypingStoryPanel;
import ui.GameUiFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.ArrayList;
import java.util.List;

public class StageResultStoryScreen {
    private static final String STORY_FONT_FAMILY = "Georgia";
    private static final String DIALOGUE_FONT_FAMILY = "Trebuchet MS";
    private static final int DIALOGUE_TEXT_WIDTH = 880;
    private static final int DIALOGUE_BOX_HEIGHT = 144;
    private static final int DIALOGUE_MAX_PAGE_CHARS = 175;
    private static final int TYPE_INTERVAL_MILLIS = 25;
    private static final int TYPE_CHARS_PER_TICK = 1;
    private static final Color BLUE_TOP = new Color(9, 28, 48);
    private static final Color BLUE_BOTTOM = new Color(2, 7, 14);
    private static final Color PANEL_FILL = new Color(5, 14, 25, 182);
    private static final Color PANEL_BORDER = new Color(119, 169, 205, 95);
    private static final Color GOLD = new Color(224, 184, 96);
    private static final Color TEXT = new Color(236, 244, 247);
    private static final Color TEXT_MUTED = new Color(164, 197, 213);
    private static final Color DIALOGUE_BOX_COLOR = new Color(8, 10, 14);

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
        if (usesCenteredResultStory()) {
            return createCenteredResultStory();
        }

        JPanel root = new StoryBackgroundPanel(
                resultBackgroundTop(),
                resultBackgroundBottom(),
                loadStageBackgroundAsset()
        );
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

        List<String> pages = storyPages(StageResultStoryCatalog.storyFor(playableStage, scoreTracker));
        int[] pageIndex = {0};
        JTextPane story = centeredTextPane(pages.get(pageIndex[0]));
        story.setBounds(66, 88, 448, 126);
        narrativePanel.add(story);
        root.add(narrativePanel);

        JLabel prompt = centeredLabel(promptText(pageIndex[0], pages.size()), TEXT_MUTED, Font.BOLD, 15);
        prompt.setBounds(0, 522, 1000, 30);
        root.add(prompt);

        JButton nextButton = GameUiFactory.createSmallButton("NEXT");
        nextButton.setBounds(440, 552, 120, 40);
        root.add(nextButton);

        AbstractAction advanceStory = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageIndex[0] < pages.size() - 1) {
                    pageIndex[0]++;
                    setCenteredText(story, pages.get(pageIndex[0]));
                    story.setFont(new Font(STORY_FONT_FAMILY, Font.PLAIN, storyFontSize(pages.get(pageIndex[0]))));
                    prompt.setText(promptText(pageIndex[0], pages.size()));
                    return;
                }

                controller.showResults(playableStage, scoreTracker);
            }
        };
        nextButton.addActionListener(advanceStory);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "showResults");
        root.getActionMap().put("showResults", advanceStory);

        return root;
    }

    private JPanel createCenteredResultStory() {
        AnimatedGifBackground stageBackground = loadStageBackgroundAsset();

        return new CenteredTypingStoryPanel(
                stageBackground,
                resultBackgroundTop(),
                resultBackgroundBottom(),
                centeredEpiloguePages(StageResultStoryCatalog.storyFor(playableStage, scoreTracker), "Narrator"),
                () -> controller.showResults(playableStage, scoreTracker)
        );
    }

    private boolean usesCenteredResultStory() {
        return playableStage instanceof WaveStage || playableStage instanceof LittleBellStage;
    }

    private void startDialoguePage(
            JLabel speaker,
            Component speakerGap,
            JTextArea lore,
            JLabel prompt,
            List<DialoguePage> pages,
            int pageIndex,
            String[] fullPageText,
            int[] visibleCharacters,
            Timer[] typingTimer
    ) {
        stopTyping(typingTimer);

        DialoguePage page = pages.get(pageIndex);
        boolean narration = page.speaker().equalsIgnoreCase("Narrator");
        speaker.setVisible(!narration);
        speakerGap.setVisible(!narration);
        speaker.setText(narration ? "" : page.speaker());
        fullPageText[0] = displayTextFor(page);
        visibleCharacters[0] = 0;
        lore.setText("");
        prompt.setText(promptText(pageIndex, pages.size()));
        lore.setCaretPosition(0);
        lore.revalidate();
        speaker.repaint();
        lore.repaint();
        prompt.repaint();

        typingTimer[0] = new Timer(
                TYPE_INTERVAL_MILLIS,
                e -> revealNextTextChunk(lore, fullPageText, visibleCharacters, typingTimer)
        );
        typingTimer[0].start();
    }

    private void revealNextTextChunk(
            JTextArea lore,
            String[] fullPageText,
            int[] visibleCharacters,
            Timer[] typingTimer
    ) {
        visibleCharacters[0] = Math.min(fullPageText[0].length(), visibleCharacters[0] + TYPE_CHARS_PER_TICK);
        lore.setText(fullPageText[0].substring(0, visibleCharacters[0]));
        lore.setCaretPosition(0);

        if (visibleCharacters[0] >= fullPageText[0].length()) {
            stopTyping(typingTimer);
        }
    }

    private void completeDialoguePage(
            JTextArea lore,
            String[] fullPageText,
            int[] visibleCharacters,
            Timer[] typingTimer
    ) {
        stopTyping(typingTimer);
        visibleCharacters[0] = fullPageText[0].length();
        lore.setText(fullPageText[0]);
        lore.setCaretPosition(0);
        lore.repaint();
    }

    private void stopTyping(Timer[] typingTimer) {
        if (typingTimer[0] != null) {
            typingTimer[0].stop();
            typingTimer[0] = null;
        }
    }

    private String displayTextFor(DialoguePage page) {
        if (!page.quoted()) {
            return page.text();
        }

        return quoteDialogueText(page.text());
    }

    private String quoteDialogueText(String text) {
        String[] lines = text.strip().split("\\R", -1);
        StringBuilder quotedText = new StringBuilder();

        for (String line : lines) {
            if (!quotedText.isEmpty()) {
                quotedText.append('\n');
            }

            String trimmedLine = line.strip();

            if (trimmedLine.isBlank() || isStageDirection(trimmedLine) || isQuoted(trimmedLine)) {
                quotedText.append(trimmedLine);
            } else {
                quotedText.append('"').append(trimmedLine).append('"');
            }
        }

        return quotedText.toString();
    }

    private boolean isStageDirection(String text) {
        return text.startsWith("(") && text.endsWith(")");
    }

    private boolean isQuoted(String text) {
        return text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"");
    }

    private List<DialoguePage> dialoguePages(String story, String defaultSpeaker) {
        if (story.isBlank()) {
            return List.of(new DialoguePage(defaultSpeaker, "", false));
        }

        List<DialoguePage> pages = new ArrayList<>();

        for (String block : story.split("\\R\\s*\\R")) {
            SpeakerBlock speakerBlock = parseSpeakerBlock(block, defaultSpeaker);
            for (String pageText : splitDialogueIntoPages(speakerBlock.text())) {
                boolean quoted = speakerBlock.explicitSpeaker()
                        && !speakerBlock.speaker().equalsIgnoreCase("Narrator");
                pages.add(new DialoguePage(speakerBlock.speaker(), pageText, quoted));
            }
        }

        return pages.isEmpty() ? List.of(new DialoguePage(defaultSpeaker, "", false)) : pages;
    }

    private List<String> centeredDialoguePages(String story, String defaultSpeaker) {
        List<String> centeredPages = new ArrayList<>();

        for (DialoguePage page : dialoguePages(story, defaultSpeaker)) {
            centeredPages.add(centeredTextFor(page));
        }

        return centeredPages;
    }

    private List<String> centeredEpiloguePages(String story, String defaultSpeaker) {
        List<String> centeredPages = new ArrayList<>();

        for (DialoguePage page : dialoguePages(story, defaultSpeaker)) {
            centeredPages.add(displayTextFor(page));
        }

        return centeredPages;
    }

    private String centeredTextFor(DialoguePage page) {
        String text = displayTextFor(page);

        if (page.speaker().equalsIgnoreCase("Narrator")) {
            return text;
        }

        return page.speaker() + "\n\n" + text;
    }

    private SpeakerBlock parseSpeakerBlock(String block, String defaultSpeaker) {
        String trimmedBlock = block.strip();
        int firstLineEnd = trimmedBlock.indexOf('\n');

        if (firstLineEnd > 0 && trimmedBlock.substring(0, firstLineEnd).endsWith(":")) {
            return new SpeakerBlock(
                    trimmedBlock.substring(0, firstLineEnd - 1),
                    trimmedBlock.substring(firstLineEnd + 1).strip(),
                    true
            );
        }

        return new SpeakerBlock(defaultSpeaker, trimmedBlock, false);
    }

    private List<String> splitDialogueIntoPages(String text) {
        List<String> pages = new ArrayList<>();
        String remaining = text.strip();

        while (remaining.length() > DIALOGUE_MAX_PAGE_CHARS) {
            int splitIndex = pageSplitIndex(remaining, DIALOGUE_MAX_PAGE_CHARS);
            pages.add(remaining.substring(0, splitIndex).strip());
            remaining = remaining.substring(splitIndex).strip();
        }

        if (!remaining.isBlank()) {
            pages.add(remaining);
        }

        return pages;
    }

    private AnimatedGifBackground loadStageBackgroundAsset() {
        return AnimatedGifBackground.load(AssetCatalog.gameplayBackgroundUrlFor(playableStage));
    }

    private Color resultBackgroundTop() {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> new Color(245, 193, 108);
                case UNDER_THE_TABLE -> new Color(43, 29, 24);
                case RAIN_ALLEY -> new Color(38, 59, 86);
                case WINDOW_LIGHT -> new Color(24, 34, 59);
                case THRESHOLD_LIGHT -> new Color(164, 117, 55);
                case LITTLE_BELL -> new Color(212, 138, 112);
            };
        }

        return BLUE_TOP;
    }

    private Color resultBackgroundBottom() {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> new Color(107, 63, 42);
                case UNDER_THE_TABLE -> new Color(9, 7, 6);
                case RAIN_ALLEY -> new Color(9, 17, 28);
                case WINDOW_LIGHT -> new Color(60, 36, 28);
                case THRESHOLD_LIGHT -> new Color(52, 38, 31);
                case LITTLE_BELL -> new Color(90, 49, 69);
            };
        }

        return BLUE_BOTTOM;
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
        setCenteredText(textPane, text);
        return textPane;
    }

    private void setCenteredText(JTextPane textPane, String text) {
        textPane.setText(text);
        StyledDocument document = textPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attributes, 0.06f);
        document.setParagraphAttributes(0, document.getLength(), attributes, false);
        textPane.setCaretPosition(0);
        textPane.revalidate();
        textPane.repaint();
    }

    private String promptText(int pageIndex, int pageCount) {
        if (pageCount <= 1) {
            return "press space or next";
        }

        return "press space or next  " + (pageIndex + 1) + "/" + pageCount;
    }

    private List<String> storyPages(String story) {
        List<String> pages = new ArrayList<>();
        String remaining = story.strip();
        int maxPageChars = 320;

        while (remaining.length() > maxPageChars) {
            int splitIndex = pageSplitIndex(remaining, maxPageChars);
            pages.add(remaining.substring(0, splitIndex).strip());
            remaining = remaining.substring(splitIndex).strip();
        }

        if (!remaining.isBlank()) {
            pages.add(remaining);
        }

        return pages.isEmpty() ? List.of("") : pages;
    }

    private int pageSplitIndex(String text, int maxPageChars) {
        int searchFrom = Math.min(maxPageChars, text.length() - 1);

        for (int i = searchFrom; i >= maxPageChars / 2; i--) {
            char current = text.charAt(i);
            if ((current == '.' || current == '!' || current == '?' || current == '"') && hasFollowingWhitespace(text, i)) {
                return i + 1;
            }
        }

        for (int i = searchFrom; i >= maxPageChars / 2; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }

        return maxPageChars;
    }

    private boolean hasFollowingWhitespace(String text, int index) {
        return index + 1 >= text.length() || Character.isWhitespace(text.charAt(index + 1));
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

    private static final class WavesStoryBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color topColor;
        private final Color bottomColor;
        private final AnimatedGifBackground backgroundImage;
        private final Timer animationTimer;

        private WavesStoryBackgroundPanel(Color topColor, Color bottomColor, AnimatedGifBackground backgroundImage) {
            this.topColor = topColor;
            this.bottomColor = bottomColor;
            this.backgroundImage = backgroundImage;
            setOpaque(true);

            if (backgroundImage != null && backgroundImage.isAnimated()) {
                animationTimer = new Timer(33, e -> repaint());
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
            g.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
            g.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                drawCoverImage(g, frame);
                g.setColor(new Color(2, 8, 18, 92));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 24), 0, getHeight(), new Color(0, 0, 0, 150)));
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

    private static final class WavesStoryVisualPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final boolean drawWavesFallback;

        private WavesStoryVisualPanel(boolean drawWavesFallback) {
            this.drawWavesFallback = drawWavesFallback;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (!drawWavesFallback) {
                return;
            }

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawOceanSilhouette(g);
            g.dispose();
        }

        private void drawOceanSilhouette(Graphics2D g) {
            int width = getWidth();
            int height = getHeight();
            int horizon = Math.max(140, height / 2);

            g.setPaint(new GradientPaint(0, 0, new Color(9, 32, 55, 90), 0, height, new Color(4, 9, 18, 150)));
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

    private record SpeakerBlock(String speaker, String text, boolean explicitSpeaker) {
    }

    private record DialoguePage(String speaker, String text, boolean quoted) {
    }

    private static final class StoryBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color topColor;
        private final Color bottomColor;
        private final AnimatedGifBackground backgroundImage;
        private final Timer animationTimer;

        private StoryBackgroundPanel(
                Color topColor,
                Color bottomColor,
                AnimatedGifBackground backgroundImage
        ) {
            this.topColor = topColor;
            this.bottomColor = bottomColor;
            this.backgroundImage = backgroundImage;
            setOpaque(true);

            if (backgroundImage != null && backgroundImage.isAnimated()) {
                animationTimer = new Timer(33, e -> repaint());
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
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
            g.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                drawCoverImage(g, frame);
                g.setColor(new Color(0, 0, 0, 118));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 24), 0, getHeight(), new Color(0, 0, 0, 170)));
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(new Color(60, 132, 178, 24));
                g.fill(new Ellipse2D.Double(245, 95, 510, 410));
                g.setColor(new Color(0, 0, 0, 92));
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            drawSideDecorations(g);
            drawVignette(g);
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
