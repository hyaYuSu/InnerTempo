package screens;

import content.JourneyCatalog;
import config.AssetCatalog;
import manager.ScreenManager;
import model.Journey;
import model.JourneyScene;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import ui.AnimatedGifBackground;
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
import javax.swing.KeyStroke;
import javax.swing.Timer;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoreScreen {
    private static final String STORY_FONT_FAMILY = "Trebuchet MS";
    private static final int LORE_TEXT_WIDTH = 880;
    private static final int TEXT_BOX_HEIGHT = 144;
    private static final int MAX_PAGE_CHARS = 175;
    private static final int TYPE_INTERVAL_MILLIS = 25;
    private static final int TYPE_CHARS_PER_TICK = 1;
    private static final Color TEXT_BOX_COLOR = new Color(8, 10, 14);

    private final ScreenManager controller;
    private final PlayableStage playableStage;

    public LoreScreen(ScreenManager controller, PlayableStage playableStage) {
        this.controller = controller;
        this.playableStage = playableStage;
    }

    public JPanel create() {
        Journey journey = JourneyCatalog.byId(playableStage.getJourneyId());
        AnimatedGifBackground[] stageBackgrounds = loadStageBackgroundAssets();
        boolean hasStageBackground = stageBackgrounds.length > 0;

        LoreBackgroundPanel root = new LoreBackgroundPanel(backgroundTop(), backgroundBottom(), stageBackgrounds);
        root.setLayout(new BorderLayout());
        root.setFocusable(true);

        JPanel centerContent = new LoreVisualPanel(
                playableStage instanceof LittleBellStage && !hasStageBackground,
                !(playableStage instanceof LittleBellStage) && !hasStageBackground
        );

        JLabel speaker = GameUiFactory.createLabel(
                speakerName(journey),
                new Color(218, 165, 32),
                new Font(STORY_FONT_FAMILY, Font.BOLD, 18)
        );
        JTextArea lore = GameUiFactory.createWrappedText(
                "",
                Color.WHITE,
                new Font(STORY_FONT_FAMILY, Font.PLAIN, 19),
                LORE_TEXT_WIDTH
        );
        JLabel prompt = GameUiFactory.createLabel(
                "SPACE",
                Color.LIGHT_GRAY,
                new Font(STORY_FONT_FAMILY, Font.BOLD, 14)
        );
        JButton nextButton = GameUiFactory.createSmallButton("NEXT");
        speaker.setAlignmentX(Component.LEFT_ALIGNMENT);
        lore.setAlignmentX(Component.LEFT_ALIGNMENT);
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        speaker.setOpaque(true);
        lore.setOpaque(true);
        prompt.setOpaque(true);
        speaker.setBackground(TEXT_BOX_COLOR);
        lore.setBackground(TEXT_BOX_COLOR);
        prompt.setBackground(TEXT_BOX_COLOR);

        JPanel textContent = new JPanel();
        textContent.setOpaque(true);
        textContent.setBackground(TEXT_BOX_COLOR);
        textContent.setLayout(new BoxLayout(textContent, BoxLayout.Y_AXIS));
        textContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        textContent.add(speaker);
        Component speakerGap = Box.createVerticalStrut(3);
        textContent.add(speakerGap);
        textContent.add(lore);
        textContent.add(Box.createVerticalStrut(4));
        textContent.add(prompt);

        JPanel textBox = new JPanel(new BorderLayout());
        textBox.setOpaque(true);
        textBox.setBackground(TEXT_BOX_COLOR);
        textBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        textBox.setPreferredSize(new Dimension(1000, TEXT_BOX_HEIGHT));
        textBox.add(textContent, BorderLayout.CENTER);
        textBox.add(nextButton, BorderLayout.EAST);

        root.add(textBox, BorderLayout.SOUTH);
        root.add(centerContent, BorderLayout.CENTER);

        List<LorePage> pages = lorePages(speakerName(journey));
        int[] lineIndex = {0};
        String[] fullPageText = {""};
        int[] visibleCharacters = {0};
        Timer[] typingTimer = {null};
        startLorePage(root, speaker, speakerGap, lore, prompt, pages, lineIndex[0], fullPageText, visibleCharacters, typingTimer);

        Runnable advanceLore = () -> {
            if (visibleCharacters[0] < fullPageText[0].length()) {
                completeLorePage(lore, fullPageText, visibleCharacters, typingTimer);
                return;
            }

            lineIndex[0]++;

            if (lineIndex[0] >= pages.size()) {
                stopTyping(typingTimer);
                controller.startStage(playableStage);
                return;
            }

            startLorePage(root, speaker, speakerGap, lore, prompt, pages, lineIndex[0], fullPageText, visibleCharacters, typingTimer);
        };
        nextButton.addActionListener(e -> advanceLore.run());

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "nextLoreLine");
        root.getActionMap().put("nextLoreLine", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                advanceLore.run();
            }
        });

        return root;
    }

    private void startLorePage(
            LoreBackgroundPanel backgroundPanel,
            JLabel speaker,
            Component speakerGap,
            JTextArea lore,
            JLabel prompt,
            List<LorePage> pages,
            int pageIndex,
            String[] fullPageText,
            int[] visibleCharacters,
            Timer[] typingTimer
    ) {
        stopTyping(typingTimer);
        LorePage page = pages.get(pageIndex);
        backgroundPanel.setImageCue(page.imageCue());
        boolean narration = page.speaker().equalsIgnoreCase("Narrator");
        speaker.setVisible(!narration);
        speakerGap.setVisible(!narration);
        speaker.setText(narration ? "" : page.speaker());
        fullPageText[0] = displayTextFor(page);
        visibleCharacters[0] = 0;
        lore.setText("");
        prompt.setText("press space or next  " + (pageIndex + 1) + "/" + pages.size());
        lore.setCaretPosition(0);
        lore.revalidate();
        speaker.repaint();
        lore.repaint();
        prompt.repaint();

        typingTimer[0] = new Timer(TYPE_INTERVAL_MILLIS, e -> revealNextTextChunk(lore, fullPageText, visibleCharacters, typingTimer));
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

    private void completeLorePage(
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

    private String displayTextFor(LorePage page) {
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

    private List<LorePage> lorePages(String defaultSpeaker) {
        JourneyScene scene = currentScene();
        if (scene.getStoryText().isBlank()) {
            return List.of(new LorePage(defaultSpeaker, scene.getSummary(), false, 0));
        }

        List<LorePage> pages = new ArrayList<>();
        int blockIndex = 0;

        for (String block : scene.getStoryText().split("\\R\\s*\\R")) {
            SpeakerBlock speakerBlock = parseSpeakerBlock(block, defaultSpeaker);
            List<String> splitPages = splitIntoPages(speakerBlock.text());
            for (int splitPageIndex = 0; splitPageIndex < splitPages.size(); splitPageIndex++) {
                String pageText = splitPages.get(splitPageIndex);
                boolean quoted = speakerBlock.explicitSpeaker()
                        && !speakerBlock.speaker().equalsIgnoreCase("Narrator");
                pages.add(new LorePage(
                        speakerBlock.speaker(),
                        pageText,
                        quoted,
                        imageCueForParagraph(blockIndex, splitPageIndex)
                ));
            }

            blockIndex++;
        }

        return pages.isEmpty() ? List.of(new LorePage(defaultSpeaker, scene.getSummary(), false, 0)) : pages;
    }

    private int imageCueForParagraph(int paragraphIndex, int splitPageIndex) {
        int cueIndex = Math.max(paragraphIndex, paragraphIndex + splitPageIndex);

        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case UNDER_THE_TABLE -> cueIndex < 3 ? 0 : 1;
                case LITTLE_BELL -> {
                    if (cueIndex <= 1) {
                        yield 0;
                    }
                    if (cueIndex <= 3) {
                        yield 1;
                    }
                    if (cueIndex <= 5) {
                        yield 2;
                    }
                    if (cueIndex <= 7) {
                        yield 3;
                    }
                    yield 4;
                }
                default -> 0;
            };
        }

        return 0;
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

    private List<String> splitIntoPages(String text) {
        List<String> pages = new ArrayList<>();
        String remaining = text.strip();

        while (remaining.length() > MAX_PAGE_CHARS) {
            int splitIndex = pageSplitIndex(remaining);
            pages.add(remaining.substring(0, splitIndex).strip());
            remaining = remaining.substring(splitIndex).strip();
        }

        if (!remaining.isBlank()) {
            pages.add(remaining);
        }

        return pages;
    }

    private int pageSplitIndex(String text) {
        int searchFrom = Math.min(MAX_PAGE_CHARS, text.length() - 1);

        for (int i = searchFrom; i >= MAX_PAGE_CHARS / 2; i--) {
            char current = text.charAt(i);
            if ((current == '.' || current == '!' || current == '?' || current == '"') && hasFollowingWhitespace(text, i)) {
                return i + 1;
            }
        }

        for (int i = searchFrom; i >= MAX_PAGE_CHARS / 2; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }

        return MAX_PAGE_CHARS;
    }

    private boolean hasFollowingWhitespace(String text, int index) {
        return index + 1 >= text.length() || Character.isWhitespace(text.charAt(index + 1));
    }

    private JourneyScene currentScene() {
        Journey journey = JourneyCatalog.byId(playableStage.getJourneyId());

        for (JourneyScene scene : journey.getScenes()) {
            if (scene.getNumber() == playableStage.getNumber()) {
                return scene;
            }
        }

        throw new IllegalStateException("No scene found for stage " + playableStage.getTitle());
    }

    private Color backgroundTop() {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> new Color(245, 193, 108);
                case UNDER_THE_TABLE -> new Color(43, 29, 24);
                case RAIN_ALLEY -> new Color(38, 59, 86);
                case WINDOW_LIGHT -> new Color(24, 34, 59);
                case LITTLE_BELL -> new Color(212, 138, 112);
            };
        }

        return new Color(17, 47, 74);
    }

    private Color backgroundBottom() {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> new Color(107, 63, 42);
                case UNDER_THE_TABLE -> new Color(9, 7, 6);
                case RAIN_ALLEY -> new Color(9, 17, 28);
                case WINDOW_LIGHT -> new Color(60, 36, 28);
                case LITTLE_BELL -> new Color(90, 49, 69);
            };
        }

        return new Color(6, 17, 31);
    }

    private String speakerName(Journey journey) {
        if (playableStage instanceof LittleBellStage) {
            return "Wandering Cat";
        }

        return journey.getTitle().equals("Waves") ? "Me" : journey.getTitle();
    }

    private AnimatedGifBackground[] loadStageBackgroundAssets() {
        URL[] urls = AssetCatalog.backgroundUrlsFor(playableStage);
        List<AnimatedGifBackground> backgrounds = new ArrayList<>();

        for (URL url : urls) {
            AnimatedGifBackground background = AnimatedGifBackground.load(url);
            if (background != null) {
                backgrounds.add(background);
            }
        }

        return backgrounds.toArray(AnimatedGifBackground[]::new);
    }

    private static final class LoreBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color topColor;
        private final Color bottomColor;
        private final AnimatedGifBackground[] backgroundImages;
        private final Timer animationTimer;
        private int storyFrameIndex;

        private LoreBackgroundPanel(Color topColor, Color bottomColor, AnimatedGifBackground[] backgroundImages) {
            this.topColor = topColor;
            this.bottomColor = bottomColor;
            this.backgroundImages = backgroundImages;
            setOpaque(true);

            if (hasAnimatedBackground(backgroundImages)) {
                animationTimer = new Timer(33, e -> repaint());
                animationTimer.start();
            } else {
                animationTimer = null;
            }
        }

        private void setImageCue(int imageCue) {
            if (backgroundImages.length == 0) {
                storyFrameIndex = 0;
                return;
            }

            storyFrameIndex = Math.max(0, Math.min(imageCue, backgroundImages.length - 1));
            repaint();
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

            AnimatedGifBackground selectedBackground = backgroundImages.length == 0 ? null : backgroundImages[storyFrameIndex];
            BufferedImage frame = selectedBackground == null ? null : selectedBackground.currentFrame();
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

        private static boolean hasAnimatedBackground(AnimatedGifBackground[] backgroundImages) {
            for (AnimatedGifBackground backgroundImage : backgroundImages) {
                if (backgroundImage != null && backgroundImage.isAnimated()) {
                    return true;
                }
            }

            return false;
        }
    }

    private static final class LoreVisualPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final boolean littleBell;
        private final boolean drawWavesFallback;

        private LoreVisualPanel(boolean littleBell, boolean drawWavesFallback) {
            this.littleBell = littleBell;
            this.drawWavesFallback = drawWavesFallback;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (littleBell) {
                drawRoomSilhouette(g);
            } else if (drawWavesFallback) {
                drawOceanSilhouette(g);
            }

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

        private void drawRoomSilhouette(Graphics2D g) {
            int width = getWidth();
            int height = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(90, 55, 34, 105), 0, height, new Color(22, 12, 8, 135)));
            g.fillRect(0, 0, width, height);

            int windowX = width / 2 - 130;
            int windowY = 80;
            g.setColor(new Color(255, 210, 136, 80));
            g.fill(new RoundRectangle2D.Double(windowX, windowY, 260, 154, 16, 16));
            g.setColor(new Color(34, 20, 15, 120));
            g.setStroke(new BasicStroke(5f));
            g.draw(new RoundRectangle2D.Double(windowX, windowY, 260, 154, 16, 16));
            g.draw(new Line2D.Double(windowX + 130, windowY, windowX + 130, windowY + 154));
            g.draw(new Line2D.Double(windowX, windowY + 77, windowX + 260, windowY + 77));

            int bodyX = width / 2 - 38;
            int bodyY = height / 2 + 38;
            g.setColor(new Color(12, 10, 9, 190));
            g.fill(new Ellipse2D.Double(bodyX, bodyY, 76, 52));
            g.fill(new Ellipse2D.Double(bodyX + 16, bodyY - 34, 44, 44));
            g.fillPolygon(new int[] {bodyX + 20, bodyX + 30, bodyX + 38}, new int[] {bodyY - 20, bodyY - 50, bodyY - 20}, 3);
            g.fillPolygon(new int[] {bodyX + 38, bodyX + 48, bodyX + 56}, new int[] {bodyY - 20, bodyY - 50, bodyY - 20}, 3);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(bodyX + 70, bodyY + 30, bodyX + 120, bodyY - 6));
        }
    }

    private record SpeakerBlock(String speaker, String text, boolean explicitSpeaker) {
    }

    private record LorePage(String speaker, String text, boolean quoted, int imageCue) {
    }
}
