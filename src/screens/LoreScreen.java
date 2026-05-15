package screens;

import content.JourneyCatalog;
import manager.ScreenManager;
import model.Journey;
import model.JourneyScene;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import ui.GameUiFactory;
import ui.GradientPanel;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
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

        JPanel root = new GradientPanel(backgroundTop(), backgroundBottom());
        root.setLayout(new BorderLayout());
        root.setFocusable(true);

        JLabel bgText = GameUiFactory.createLabel(
                backgroundLabel(journey),
                backgroundTextColor(),
                new Font(STORY_FONT_FAMILY, Font.PLAIN, 30)
        );
        bgText.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JLabel character = GameUiFactory.createLabel(
                characterSymbol(),
                Color.LIGHT_GRAY,
                new Font(STORY_FONT_FAMILY, Font.PLAIN, 100)
        );
        character.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBorder(BorderFactory.createEmptyBorder(70, 80, 70, 80));
        centerContent.add(bgText);
        centerContent.add(Box.createVerticalStrut(40));
        centerContent.add(character);

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

        root.add(textBox, BorderLayout.SOUTH);
        root.add(centerContent, BorderLayout.CENTER);

        List<LorePage> pages = lorePages(speakerName(journey));
        int[] lineIndex = {0};
        String[] fullPageText = {""};
        int[] visibleCharacters = {0};
        Timer[] typingTimer = {null};
        startLorePage(speaker, speakerGap, lore, prompt, pages, lineIndex[0], fullPageText, visibleCharacters, typingTimer);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "nextLoreLine");
        root.getActionMap().put("nextLoreLine", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                startLorePage(speaker, speakerGap, lore, prompt, pages, lineIndex[0], fullPageText, visibleCharacters, typingTimer);
            }
        });

        return root;
    }

    private void startLorePage(
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
        boolean narration = page.speaker().equalsIgnoreCase("Narrator");
        speaker.setVisible(!narration);
        speakerGap.setVisible(!narration);
        speaker.setText(narration ? "" : page.speaker());
        fullPageText[0] = displayTextFor(page);
        visibleCharacters[0] = 0;
        lore.setText("");
        prompt.setText("SPACE  " + (pageIndex + 1) + "/" + pages.size());
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
            return List.of(new LorePage(defaultSpeaker, scene.getSummary(), false));
        }

        List<LorePage> pages = new ArrayList<>();

        for (String block : scene.getStoryText().split("\\R\\s*\\R")) {
            SpeakerBlock speakerBlock = parseSpeakerBlock(block, defaultSpeaker);
            for (String pageText : splitIntoPages(speakerBlock.text())) {
                boolean quoted = speakerBlock.explicitSpeaker()
                        && !speakerBlock.speaker().equalsIgnoreCase("Narrator");
                pages.add(new LorePage(speakerBlock.speaker(), pageText, quoted));
            }
        }

        return pages.isEmpty() ? List.of(new LorePage(defaultSpeaker, scene.getSummary(), false)) : pages;
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

    private String backgroundLabel(Journey journey) {
        if (playableStage instanceof LittleBellStage littleBellStage) {
            return switch (littleBellStage) {
                case EMPTY_BASKET -> "Sunny Room Background";
                case UNDER_THE_TABLE -> "Under Table Background";
                case RAIN_ALLEY -> "Rain Alley Background";
                case WINDOW_LIGHT -> "Window Light Background";
                case LITTLE_BELL -> "Golden-Pink Doorstep Background";
            };
        }

        return journey.getBackgroundLabel();
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

    private Color backgroundTextColor() {
        if (playableStage instanceof LittleBellStage) {
            return new Color(255, 228, 181);
        }

        return new Color(173, 216, 230);
    }

    private String characterSymbol() {
        if (playableStage instanceof LittleBellStage) {
            return "(=^.^=)";
        }

        return "\u25CF";
    }

    private String speakerName(Journey journey) {
        if (playableStage instanceof LittleBellStage) {
            return "Wandering Cat";
        }

        return journey.getTitle().equals("Waves") ? "Me" : journey.getTitle();
    }

    private record SpeakerBlock(String speaker, String text, boolean explicitSpeaker) {
    }

    private record LorePage(String speaker, String text, boolean quoted) {
    }
}
