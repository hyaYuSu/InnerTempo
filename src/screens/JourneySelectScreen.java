package screens;

import content.JourneyCatalog;
import manager.SaveManager;
import manager.ScreenManager;
import model.Journey;
import model.JourneyId;
import model.JourneyScene;
import model.Stages.LittleBellStage;
import model.Stages.PlayableStage;
import model.Stages.WaveStage;
import ui.GradientPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private JLabel detailMeta;
    private JLabel detailStatus;
    private JTextArea detailSummary;

    public JourneySelectScreen(ScreenManager controller, SaveManager saveManager, JourneyId selectedJourneyId) {
        this.controller = controller;
        this.saveManager = saveManager;
        this.selectedJourney = JourneyCatalog.byId(selectedJourneyId);
    }

    public JPanel create() {
        JPanel root = new GradientPanel(BACKGROUND_TOP, BACKGROUND_BOTTOM);
        root.setLayout(new BorderLayout());

        root.add(createJourneyMenu(), BorderLayout.WEST);
        root.add(createJourneyDetailsScroller(), BorderLayout.CENTER);

        return root;
    }

    private JScrollPane createJourneyDetailsScroller() {
        JScrollPane scrollPane = new JScrollPane(
                createJourneyDetails(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        styleScrollPane(scrollPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        return scrollPane;
    }

    private JPanel createJourneyMenu() {
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 42, 50, 28));
        leftPanel.setPreferredSize(new Dimension(270, 600));

        JLabel playTitle = createLabel("PLAY", TEXT, DISPLAY_FONT);
        JLabel pickStage = createLabel("SELECT JOURNEY", TEXT_MUTED, new Font("Georgia", Font.BOLD, 13));

        JButton waves = createRailButton("WAVES", selectedJourney.getId() == JourneyId.WAVES);
        JButton stage2 = createRailButton("LITTLE BELL", selectedJourney.getId() == JourneyId.GATO);
        JButton comingSoon = createRailButton("COMING SOON", false);
        JButton backButton = createRailButton("BACK", false);

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

        JPanel rightPanel = new ScrollableDetailsPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(52, 36, 42, 56));

        JLabel title = createLabel(selectedJourney.getTitle(), GOLD, TITLE_FONT);
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
        rightPanel.add(Box.createVerticalStrut(8));
        addLeftAligned(rightPanel, subtitle);
        rightPanel.add(Box.createVerticalStrut(10));
        addLeftAligned(rightPanel, description);
        rightPanel.add(Box.createVerticalStrut(14));
        addLeftAligned(rightPanel, progress);
        rightPanel.add(Box.createVerticalStrut(24));
        addLeftAligned(rightPanel, createSceneScroller());
        rightPanel.add(Box.createVerticalStrut(24));
        addLeftAligned(rightPanel, createDetailPanel());

        selectScene(sceneCards.get(0), selectedJourney.getScenes().get(0), isUnlocked(selectedJourney.getScenes().get(0)));
        return rightPanel;
    }

    private JScrollPane createSceneScroller() {
        JPanel cardsRow = new JPanel();
        cardsRow.setOpaque(false);
        cardsRow.setLayout(new BoxLayout(cardsRow, BoxLayout.X_AXIS));
        cardsRow.setBorder(BorderFactory.createEmptyBorder(8, 8, 16, 8));

        for (JourneyScene scene : selectedJourney.getScenes()) {
            boolean unlocked = isUnlocked(scene);
            SceneCard card = new SceneCard(scene, unlocked);
            sceneCards.add(card);

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    selectScene(card, scene, unlocked);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectScene(card, scene, unlocked);
                    if (unlocked) {
                        openScene(scene);
                    }
                }
            });

            cardsRow.add(card);
            cardsRow.add(Box.createHorizontalStrut(16));
        }

        JScrollPane scrollPane = new JScrollPane(
                cardsRow,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        styleScrollPane(scrollPane);
        scrollPane.setPreferredSize(new Dimension(820, 232));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 232));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(26);
        return scrollPane;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new CinematicPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        panel.setPreferredSize(new Dimension(820, 190));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        detailKicker = createLabel("", GOLD_SOFT, META_FONT);
        detailTitle = createLabel("", TEXT, new Font("Georgia", Font.BOLD, 28));
        detailMeta = createLabel("", TEXT_MUTED, new Font("Georgia", Font.BOLD, 14));
        detailSummary = createWrappedText("", TEXT, BODY_FONT, 740);
        detailStatus = createLabel("", GOLD, new Font("Georgia", Font.BOLD, 15));

        addLeftAligned(panel, detailKicker);
        panel.add(Box.createVerticalStrut(7));
        addLeftAligned(panel, detailTitle);
        panel.add(Box.createVerticalStrut(10));
        addLeftAligned(panel, detailMeta);
        panel.add(Box.createVerticalStrut(14));
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
        detailTitle.setText(scene.getTitle());
        detailMeta.setText(previewMetaText(scene, unlocked));
        detailSummary.setText(scene.getSummary());
        detailStatus.setText(unlocked ? "READY" : "LOCKED - " + scene.getLockedReason());

        detailKicker.revalidate();
        detailTitle.revalidate();
        detailMeta.revalidate();
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

    private String previewMetaText(JourneyScene scene, boolean unlocked) {
        PlayableStage playableStage = scene.getPlayableStage();

        if (playableStage == null) {
            return unlocked ? "Story scene | Unlocked" : "Story scene | Locked";
        }

        return difficultyText(playableStage)
                + " | "
                + bpmText(playableStage)
                + " | Best "
                + saveManager.getHighScore(playableStage)
                + " | "
                + (unlocked ? "Unlocked" : "Locked");
    }

    private String difficultyText(PlayableStage stage) {
        int level = Math.min(5, stage.getIndex() + 1);

        return "Difficulty " + level + "/5";
    }

    private String bpmText(PlayableStage stage) {
        if (stage == WaveStage.DRIFT) {
            return "80 BPM";
        }

        if (stage == WaveStage.FIRST_WAVES) {
            return "100 BPM";
        }

        if (stage instanceof WaveStage) {
            return "Generated rhythm";
        }

        if (stage instanceof LittleBellStage) {
            return "Story rhythm";
        }

        return "Rhythm stage";
    }

    private String progressText() {
        int unlocked = 0;

        for (JourneyScene scene : selectedJourney.getScenes()) {
            if (isUnlocked(scene)) {
                unlocked++;
            }
        }

        return unlocked + " / " + selectedJourney.getScenes().size() + " scenes unlocked";
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

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        styleScrollBar(scrollPane.getHorizontalScrollBar());
    }

    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setOpaque(false);
        scrollBar.setPreferredSize(new Dimension(8, 8));
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(70, 63, 47);
                trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    private void addLeftAligned(JPanel panel, Component component) {
        if (component instanceof JComponent swingComponent) {
            swingComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        panel.add(component);
    }

    private final class SceneCard extends JPanel {
        private static final int CARD_WIDTH = 216;
        private static final int CARD_HEIGHT = 176;

        private final JourneyScene scene;
        private final boolean unlocked;
        private boolean selected;

        private SceneCard(JourneyScene scene, boolean unlocked) {
            this.scene = scene;
            this.unlocked = unlocked;

            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 18, 20));
            setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel sceneNumber = createLabel(sceneKicker(scene), unlocked ? GOLD_SOFT : TEXT_DIM, META_FONT);
            JLabel title = createLabel(scene.getTitle(), unlocked ? TEXT : TEXT_DIM, new Font("Georgia", Font.BOLD, 22));
            JTextArea summary = createWrappedText(scene.getSummary(), unlocked ? TEXT_MUTED : TEXT_DIM, new Font("Georgia", Font.PLAIN, 13), 170);
            JLabel status = createLabel(cardStatus(scene, unlocked), unlocked ? GOLD : TEXT_DIM, new Font("Georgia", Font.BOLD, 12));

            addLeftAligned(this, sceneNumber);
            add(Box.createVerticalStrut(12));
            addLeftAligned(this, title);
            add(Box.createVerticalStrut(10));
            addLeftAligned(this, summary);
            add(Box.createVerticalGlue());
            addLeftAligned(this, status);
        }

        private void setSelectedCard(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 7;
            int y = 7;
            int width = getWidth() - 14;
            int height = getHeight() - 14;
            int arc = 18;

            if (selected) {
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), unlocked ? 52 : 28));
                g2.setStroke(new BasicStroke(5f));
                g2.drawRoundRect(x - 2, y - 2, width + 4, height + 4, arc + 4, arc + 4);
            }

            g2.setPaint(new GradientPaint(
                    0,
                    y,
                    unlocked ? PANEL_DARK : PANEL_DARKER,
                    0,
                    y + height,
                    unlocked ? new Color(8, 8, 8) : new Color(6, 6, 7)
            ));
            g2.fillRoundRect(x, y, width, height, arc, arc);

            g2.setColor(selected ? GOLD_SOFT : unlocked ? BORDER : BORDER_MUTED);
            g2.setStroke(new BasicStroke(selected ? 1.6f : 1f));
            g2.drawRoundRect(x, y, width, height, arc, arc);

            if (!unlocked) {
                g2.setColor(new Color(0, 0, 0, 72));
                g2.fillRoundRect(x, y, width, height, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        private String cardStatus(JourneyScene scene, boolean unlocked) {
            if (!unlocked) {
                return "LOCKED";
            }

            PlayableStage stage = scene.getPlayableStage();
            if (stage == null) {
                return "STORY";
            }

            return "BEST " + saveManager.getHighScore(stage);
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

    private static final class ScrollableDetailsPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(24, visibleRect.height - 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
