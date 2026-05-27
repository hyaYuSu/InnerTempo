package screens;

import content.JourneyCatalog;
import config.AssetCatalog;
import manager.SaveManager;
import manager.ScreenManager;
import model.Journey;
import model.JourneyId;
import model.JourneyScene;
import model.Stages.PlayableStage;
import ui.AnimatedGifBackground;
import ui.GameUiFactory;
import ui.MenuCardLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Locale;

public class JourneySelectScreen {
    private static final Color BACKGROUND_TOP = new Color(5, 5, 6);
    private static final Color BACKGROUND_BOTTOM = new Color(12, 11, 10);
    private static final Color PANEL_DARK = new Color(13, 13, 14);
    private static final Color PANEL_DARKER = new Color(8, 8, 9);
    private static final Color BORDER = new Color(52, 49, 43);
    private static final Color BORDER_MUTED = new Color(34, 34, 36);
    private static final Color GOLD = new Color(220, 178, 82);
    private static final Color GOLD_SOFT = new Color(178, 137, 61);
    private static final Color TEXT = new Color(238, 235, 226);
    private static final Color TEXT_MUTED = new Color(164, 160, 150);
    private static final Color TEXT_DIM = new Color(103, 101, 97);
    private static final float BACKGROUND_IMAGE_OPACITY = 0.35f;
    private static final Font DISPLAY_FONT = new Font("Georgia", Font.BOLD, 42);
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 30);
    private static final Font BODY_FONT = new Font("Georgia", Font.PLAIN, 16);
    private static final Font META_FONT = new Font("Georgia", Font.BOLD, 13);

    private final ScreenManager controller;
    private final SaveManager saveManager;
    private final Journey selectedJourney;
    private final java.util.List<SceneCard> sceneCards = new java.util.ArrayList<>();

    private SceneCard selectedCard;
    private JLabel detailKicker;
    private JLabel detailTitle;
    private JLabel detailStatus;
    private JTextArea detailSummary;

    public JourneySelectScreen(ScreenManager controller, SaveManager saveManager, JourneyId selectedJourneyId) {
        this.controller = controller;
        this.saveManager = saveManager;
        this.selectedJourney = JourneyCatalog.byId(selectedJourneyId);
    }

    public JPanel create() {
        JPanel root = new JourneyBackgroundPanel(AnimatedGifBackground.load(AssetCatalog.titleScreenUrl()));
        root.setLayout(new BorderLayout());

        root.add(createJourneyMenu(), BorderLayout.WEST);
        root.add(createJourneyDetails(), BorderLayout.CENTER);

        return root;
    }

    private JPanel createJourneyMenu() {
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(MenuCardLayout.journeyMenuBorder());
        leftPanel.setPreferredSize(MenuCardLayout.journeyMenuSize());

        JLabel playTitle = createLabel("PLAY", TEXT, DISPLAY_FONT);
        JLabel pickStage = createLabel("SELECT JOURNEY", TEXT_MUTED, new Font("Georgia", Font.BOLD, 13));

        JButton waves = createRailButton("WAVES", selectedJourney.getId() == JourneyId.WAVES);
        JButton stage2 = createRailButton("LITTLE BELL", selectedJourney.getId() == JourneyId.GATO);
        JButton comingSoon = createRailButton("COMING SOON", false);
        JButton backButton = GameUiFactory.createSmallButton("BACK");

        waves.addActionListener(e -> controller.showJourneySelect(JourneyId.WAVES));
        stage2.addActionListener(e -> controller.showJourneySelect(JourneyId.GATO));
        comingSoon.setEnabled(false);
        comingSoon.setForeground(TEXT_DIM);
        comingSoon.setCursor(Cursor.getDefaultCursor());
        backButton.addActionListener(e -> controller.showMainMenu());

        addLeftAligned(leftPanel, playTitle);
        leftPanel.add(Box.createVerticalStrut(10));
        addLeftAligned(leftPanel, pickStage);
        leftPanel.add(Box.createVerticalStrut(30));
        addLeftAligned(leftPanel, waves);
        leftPanel.add(Box.createVerticalStrut(12));
        addLeftAligned(leftPanel, stage2);
        leftPanel.add(Box.createVerticalStrut(12));
        addLeftAligned(leftPanel, comingSoon);
        leftPanel.add(Box.createVerticalGlue());
        addLeftAligned(leftPanel, backButton);

        return leftPanel;
    }

    private JPanel createJourneyDetails() {
        sceneCards.clear();
        selectedCard = null;

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(MenuCardLayout.journeyDetailsBorder());

        JLabel title = createLabel(selectedJourney.getTitle().toUpperCase(Locale.ROOT), GOLD, TITLE_FONT);
        JLabel subtitle = createLabel(
                selectedJourney.getSubtitle(),
                TEXT,
                new Font("Georgia", Font.PLAIN, 19)
        );
        JTextArea description = createWrappedText(
                selectedJourney.getDescription(),
                TEXT_MUTED,
                BODY_FONT,
                760
        );
        JLabel progress = createLabel(progressText(), GOLD_SOFT, META_FONT);

        addLeftAligned(rightPanel, title);
        rightPanel.add(Box.createVerticalStrut(6));
        addLeftAligned(rightPanel, subtitle);
        rightPanel.add(Box.createVerticalStrut(8));
        addLeftAligned(rightPanel, description);
        rightPanel.add(Box.createVerticalStrut(10));
        addLeftAligned(rightPanel, progress);
        rightPanel.add(Box.createVerticalStrut(14));
        addLeftAligned(rightPanel, createSceneGrid());
        rightPanel.add(Box.createVerticalStrut(10));
        addLeftAligned(rightPanel, createDetailPanel());

        selectScene(sceneCards.get(0), selectedJourney.getScenes().get(0), isUnlocked(selectedJourney.getScenes().get(0)));
        return rightPanel;
    }

    private JPanel createSceneGrid() {
        JPanel cardsRow = new JPanel();
        cardsRow.setOpaque(false);
        cardsRow.setLayout(new GridLayout(
                0,
                MenuCardLayout.SCENE_GRID_COLUMNS,
                MenuCardLayout.SCENE_GRID_HORIZONTAL_GAP,
                MenuCardLayout.SCENE_GRID_VERTICAL_GAP
        ));
        cardsRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cardsRow.setPreferredSize(MenuCardLayout.sceneGridSize());
        cardsRow.setMaximumSize(MenuCardLayout.sceneGridSize());

        for (JourneyScene scene : selectedJourney.getScenes()) {
            boolean unlocked = isUnlocked(scene);
            SceneCard card = new SceneCard(scene, unlocked);
            sceneCards.add(card);
            installSceneCardInteraction(card, scene, unlocked);

            cardsRow.add(card);
        }

        return cardsRow;
    }

    private void installSceneCardInteraction(SceneCard card, JourneyScene scene, boolean unlocked) {
        MouseAdapter sceneCardHandler = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectScene(card, scene, unlocked);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectScene(card, scene, unlocked);
                if (unlocked) {
                    openScene(scene);
                }
            }
        };

        attachSceneCardHandler(card, sceneCardHandler);
    }

    private void attachSceneCardHandler(Component component, MouseAdapter handler) {
        component.addMouseListener(handler);
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                attachSceneCardHandler(child, handler);
            }
        }
    }

    private AnimatedGifBackground sceneCardBackground(JourneyScene scene) {
        return AnimatedGifBackground.load(AssetCatalog.cardBackgroundUrlFor(scene.getPlayableStage()));
    }

    private JPanel createDetailPanel() {
        JPanel panel = new CinematicPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        panel.setPreferredSize(MenuCardLayout.detailPanelSize());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, MenuCardLayout.DETAIL_PANEL_HEIGHT));

        detailKicker = createLabel("", GOLD_SOFT, META_FONT);
        detailTitle = createLabel("", TEXT, new Font("Georgia", Font.BOLD, 24));
        detailSummary = createWrappedText("", TEXT, new Font("Georgia", Font.PLAIN, 14), 740);
        detailStatus = createLabel("", GOLD, new Font("Georgia", Font.BOLD, 13));
        detailSummary.setPreferredSize(new Dimension(740, 48));
        detailSummary.setMaximumSize(new Dimension(740, 52));

        addLeftAligned(panel, detailKicker);
        panel.add(Box.createVerticalStrut(4));
        addLeftAligned(panel, detailTitle);
        panel.add(Box.createVerticalStrut(8));
        addLeftAligned(panel, detailSummary);
        panel.add(Box.createVerticalGlue());
        addLeftAligned(panel, detailStatus);

        return panel;
    }

    private void selectScene(SceneCard card, JourneyScene scene, boolean unlocked) {
        if (selectedCard != null) {
            selectedCard.setSelectedCard(false);
        }

        selectedCard = card;
        selectedCard.setSelectedCard(true);

        detailKicker.setText(sceneKicker(scene));
        detailTitle.setText(scene.getTitle().toUpperCase(Locale.ROOT));
        detailSummary.setText(scene.getSummary());
        detailStatus.setText(sceneStatus(scene, unlocked));

        detailKicker.revalidate();
        detailTitle.revalidate();
        detailSummary.revalidate();
        detailStatus.revalidate();
    }

    private void openScene(JourneyScene scene) {
        if (scene.startsGameplay()) {
            controller.showLore(scene.getPlayableStage());
        } else {
            controller.showJourneyScene(selectedJourney, scene);
        }
    }

    private boolean isUnlocked(JourneyScene scene) {
        PlayableStage playableStage = scene.getPlayableStage();

        if (playableStage != null) {
            return saveManager.isUnlocked(playableStage);
        }

        return scene.isUnlockedByDefault();
    }

    private String sceneKicker(JourneyScene scene) {
        return String.format("SCENE %02d", scene.getNumber());
    }

    private String progressText() {
        int unlocked = 0;
        int cleared = 0;
        JourneyScene nextLockedScene = null;

        for (JourneyScene scene : selectedJourney.getScenes()) {
            if (isUnlocked(scene)) {
                unlocked++;
            }

            PlayableStage playableStage = scene.getPlayableStage();
            if (playableStage != null && saveManager.isCleared(playableStage)) {
                cleared++;
            }

            if (nextLockedScene == null && !isUnlocked(scene)) {
                nextLockedScene = scene;
            }
        }

        String progress = cleared + " / " + selectedJourney.getScenes().size()
                + " scenes cleared - " + unlocked + " unlocked";

        if (nextLockedScene != null) {
            progress += " - Next unlock: " + nextLockedScene.getLockedReason();
        }

        return progress;
    }

    private String sceneStatus(JourneyScene scene, boolean unlocked) {
        if (!unlocked) {
            return "LOCKED: " + scene.getLockedReason();
        }

        PlayableStage stage = scene.getPlayableStage();
        if (stage == null) {
            return "STORY";
        }

        double bestAccuracy = saveManager.getBestAccuracy(stage);
        if (saveManager.isCleared(stage)) {
            return bestAccuracy > 0
                    ? String.format("CLEARED - BEST %.1f%%", bestAccuracy)
                    : "CLEARED";
        }

        if (bestAccuracy > 0) {
            return String.format("READY - BEST %.1f%%", bestAccuracy);
        }

        return "READY";
    }

    private JButton createRailButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Georgia", Font.BOLD, 17));
        button.setForeground(selected ? GOLD : TEXT_MUTED);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(190, 42));
        button.setMinimumSize(new Dimension(190, 42));
        button.setMaximumSize(new Dimension(190, 42));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, selected ? GOLD_SOFT : BORDER_MUTED),
                BorderFactory.createEmptyBorder(0, 2, 8, 0)
        ));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setForeground(GOLD);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setForeground(selected ? GOLD : TEXT_MUTED);
                }
            }
        });

        if (!button.isEnabled()) {
            button.setForeground(TEXT_DIM);
        }

        return button;
    }

    private JLabel createLabel(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    private JTextArea createWrappedText(String text, Color color, Font font, int width) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setForeground(color);
        area.setFont(font);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setSize(new Dimension(width, Short.MAX_VALUE));
        area.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        area.setPreferredSize(new Dimension(width, area.getPreferredSize().height));
        return area;
    }

    private void addLeftAligned(JPanel panel, Component component) {
        if (component instanceof JComponent swingComponent) {
            swingComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        panel.add(component);
    }

    private static final class JourneyBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final AnimatedGifBackground backgroundImage;
        private final Timer animationTimer;

        private JourneyBackgroundPanel(AnimatedGifBackground backgroundImage) {
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
            g.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, 0, getHeight(), BACKGROUND_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                g.setComposite(AlphaComposite.SrcOver.derive(BACKGROUND_IMAGE_OPACITY));
                drawCoverImage(g, frame);
                g.setComposite(AlphaComposite.SrcOver);
                g.setColor(new Color(0, 0, 0, 122));
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

    private final class SceneCard extends JPanel {
        private static final Font CARD_TITLE_FONT = new Font("Georgia", Font.BOLD, 16);
        private static final Font CARD_TITLE_SMALL_FONT = new Font("Georgia", Font.BOLD, 14);

        private final JourneyScene scene;
        private final boolean unlocked;
        private final AnimatedGifBackground backgroundImage;
        private final Timer animationTimer;
        private boolean selected;

        private SceneCard(JourneyScene scene, boolean unlocked) {
            this.scene = scene;
            this.unlocked = unlocked;
            this.backgroundImage = sceneCardBackground(scene);

            setOpaque(false);
            setPreferredSize(MenuCardLayout.sceneCardSize());
            setMinimumSize(MenuCardLayout.sceneCardSize());
            setMaximumSize(MenuCardLayout.sceneCardSize());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (backgroundImage != null && backgroundImage.isAnimated()) {
                animationTimer = new Timer(33, e -> repaint());
                animationTimer.start();
            } else {
                animationTimer = null;
            }
        }

        private void setSelectedCard(boolean selected) {
            this.selected = selected;
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
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int x = 0;
            int y = 0;
            int width = getWidth() - 1;
            int height = getHeight() - 1;
            int arc = 14;
            RoundRectangle2D cardShape = new RoundRectangle2D.Double(x, y, width, height, arc, arc);

            if (selected) {
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), unlocked ? 52 : 28));
                g2.fill(cardShape);
            }

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                Shape previousClip = g2.getClip();
                g2.clip(cardShape);
                drawCoverImage(g2, frame, x, y, width, height);
                g2.setColor(new Color(0, 0, 0, unlocked ? 105 : 178));
                g2.fillRect(x, y, width, height);
                g2.setPaint(new GradientPaint(0, y, new Color(0, 0, 0, 20), 0, y + height, new Color(0, 0, 0, 150)));
                g2.fillRect(x, y, width, height);
                g2.setClip(previousClip);
            } else {
                g2.setPaint(new GradientPaint(
                        0,
                        y,
                        unlocked ? PANEL_DARK : PANEL_DARKER,
                        0,
                        y + height,
                        unlocked ? new Color(8, 8, 8) : new Color(6, 6, 7)
                ));
                g2.fill(cardShape);
            }

            if (!unlocked) {
                g2.setColor(new Color(0, 0, 0, 72));
                g2.fillRoundRect(x, y, width, height, arc, arc);
            }

            g2.setColor(selected ? GOLD_SOFT : unlocked ? BORDER : BORDER_MUTED);
            g2.setStroke(new BasicStroke(selected ? 2f : 1f));
            g2.draw(cardShape);

            drawCardText(g2, x, y, width, height);
            g2.dispose();
        }

        private void drawCoverImage(Graphics2D g, BufferedImage frame, int x, int y, int width, int height) {
            double scale = Math.max(width / (double) frame.getWidth(), height / (double) frame.getHeight());
            int drawWidth = (int) Math.ceil(frame.getWidth() * scale);
            int drawHeight = (int) Math.ceil(frame.getHeight() * scale);
            int drawX = x + (width - drawWidth) / 2;
            int drawY = y + (height - drawHeight) / 2;

            g.drawImage(frame, drawX, drawY, drawWidth, drawHeight, this);
        }

        private void drawCardText(Graphics2D g, int x, int y, int width, int height) {
            int left = x + 18;
            int maxTextWidth = width - 36;
            Color titleColor = unlocked ? GOLD : TEXT_DIM;
            String cardTitle = scene.getTitle().toUpperCase(Locale.ROOT);

            Font titleFont = titleFitsOnOneLine(g, cardTitle, CARD_TITLE_FONT, maxTextWidth)
                    ? CARD_TITLE_FONT
                    : CARD_TITLE_SMALL_FONT;
            g.setFont(titleFont);
            java.util.List<String> titleLines = wrapCardTitle(cardTitle, g.getFontMetrics(), maxTextWidth);
            int lineHeight = 18;
            int titleBaseline = y + 28;
            for (String line : titleLines) {
                drawShadowedText(g, line, titleFont, titleColor, left, titleBaseline);
                titleBaseline += lineHeight;
            }
        }

        private boolean titleFitsOnOneLine(Graphics2D g, String title, Font font, int maxWidth) {
            g.setFont(font);
            return g.getFontMetrics().stringWidth(title) <= maxWidth;
        }

        private java.util.List<String> wrapCardTitle(String title, java.awt.FontMetrics metrics, int maxWidth) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            StringBuilder currentLine = new StringBuilder();

            for (String word : title.split("\\s+")) {
                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (metrics.stringWidth(candidate) <= maxWidth || currentLine.isEmpty()) {
                    currentLine = new StringBuilder(candidate);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            if (lines.size() <= 2) {
                return lines;
            }

            return java.util.List.of(lines.get(0), lines.get(1));
        }

        private void drawShadowedText(Graphics2D g, String text, Font font, Color color, int x, int baselineY) {
            g.setFont(font);
            g.setColor(new Color(0, 0, 0, 170));
            g.drawString(text, x + 1, baselineY + 1);
            g.setColor(color);
            g.drawString(text, x, baselineY);
        }

    }

    private static final class CinematicPanel extends JPanel {
        private CinematicPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 0;
            int y = 0;
            int width = getWidth() - 1;
            int height = getHeight() - 1;
            int arc = 18;

            g2.setPaint(new GradientPaint(0, 0, PANEL_DARK, 0, height, PANEL_DARKER));
            g2.fillRoundRect(x, y, width, height, arc, arc);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, width, height, arc, arc);
            g2.dispose();

            super.paintComponent(g);
        }
    }

}
