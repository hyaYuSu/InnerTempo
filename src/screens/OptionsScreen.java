package screens;

import config.AssetCatalog;
import manager.ScreenManager;
import manager.SaveManager;
import settings.GameplaySettings;
import ui.AnimatedGifBackground;
import ui.GameUiFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.KeyEventDispatcher;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class OptionsScreen {
    private static final String[] LANE_NAMES = {"LEFT", "UP", "DOWN", "RIGHT"};
    private static final Color ROOT_TOP = new Color(48, 31, 20);
    private static final Color ROOT_BOTTOM = new Color(12, 8, 6);
    private static final Color INK = new Color(69, 43, 24);
    private static final Color INK_MUTED = new Color(116, 82, 48);
    private static final Color PARCHMENT = new Color(230, 207, 166);
    private static final Color PARCHMENT_DARK = new Color(177, 128, 76);
    private static final Color WOOD = new Color(110, 65, 32);
    private static final Color WOOD_DARK = new Color(72, 39, 20);
    private static final float BACKGROUND_IMAGE_OPACITY = 0.35f;
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 40);
    private static final Font LABEL_FONT = new Font("Georgia", Font.BOLD, 22);
    private static final Dimension CARD_SIZE = new Dimension(760, 540);
    private static final Dimension SLIDER_SIZE = new Dimension(644, 58);
    private static final int LANE_BUTTON_WIDTH = 195;
    private static final Dimension LANE_GRID_SIZE = new Dimension(400, 158);
    private static final int RESET_COLUMN_WIDTH = 230;

    private final ScreenManager controller;
    private final GameplaySettings options;
    private final SaveManager saveManager;
    private final Runnable backAction;
    private final AnimatedGifBackground backgroundImage;

    public OptionsScreen(ScreenManager controller, GameplaySettings options, SaveManager saveManager) {
        this(controller, options, saveManager, controller::showMainMenu);
    }

    public OptionsScreen(ScreenManager controller, GameplaySettings options, SaveManager saveManager, Runnable backAction) {
        this(controller, options, saveManager, backAction, AnimatedGifBackground.load(AssetCatalog.titleScreenUrl()));
    }

    public OptionsScreen(
            ScreenManager controller,
            GameplaySettings options,
            SaveManager saveManager,
            Runnable backAction,
            AnimatedGifBackground backgroundImage
    ) {
        this.controller = controller;
        this.options = options;
        this.saveManager = saveManager;
        this.backAction = backAction;
        this.backgroundImage = backgroundImage;
    }

    public JPanel create() {
        JPanel root = new OptionsBackgroundPanel(backgroundImage);
        root.setLayout(new GridBagLayout());

        JPanel card = new ParchmentCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(34, 58, 34, 58));
        card.setPreferredSize(CARD_SIZE);
        card.setMaximumSize(CARD_SIZE);

        JLabel title = vintageLabel("OPTIONS", TITLE_FONT, INK);
        JLabel volumeValue = vintageLabel("", LABEL_FONT, INK);
        JLabel controlsValue = vintageLabel("", new Font("Georgia", Font.BOLD, 18), INK_MUTED);
        controlsValue.setHorizontalAlignment(SwingConstants.LEFT);
        JSlider volumeSlider = createVolumeSlider();
        JButton[] laneButtons = new JButton[4];
        int[] editingLane = {-1};

        Runnable refresh = () -> {
            volumeValue.setText(options.volumeLabel());
            controlsValue.setText(editingLane[0] == -1
                    ? options.controlsLabel()
                    : "Press a key for " + LANE_NAMES[editingLane[0]]);

            for (int lane = 0; lane < laneButtons.length; lane++) {
                if (laneButtons[lane] != null) {
                    String prefix = editingLane[0] == lane ? "> " : "";
                    laneButtons[lane].setText(prefix + LANE_NAMES[lane] + ": " + options.keyLabelForLane(lane));
                }
            }
        };

        volumeSlider.addChangeListener(e -> {
            options.setMusicVolumePercent(volumeSlider.getValue());
            refresh.run();
        });

        for (int lane = 0; lane < laneButtons.length; lane++) {
            int selectedLane = lane;
            laneButtons[lane] = createControlButton();
            laneButtons[lane].addActionListener(e -> {
                editingLane[0] = selectedLane;
                refresh.run();
            });
        }

        JButton resetControls = GameUiFactory.createSmallButton("RESET");
        resetControls.addActionListener(e -> {
            options.resetControls();
            editingLane[0] = -1;
            refresh.run();
        });

        JButton resetProgress = GameUiFactory.createSmallButton("RESET PROGRESS");
        resetProgress.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    root,
                    "Reset all unlocked scenes and scores?",
                    "Reset Progress",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                saveManager.resetProgress();
            }
        });

        JButton backButton = GameUiFactory.createSmallButton("BACK");
        backButton.addActionListener(e -> backAction.run());

        refresh.run();

        addCentered(card, contentRow(title, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(24));
        addCentered(card, contentRow(volumeValue, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(8));
        addCentered(card, contentRow(volumeSlider, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(28));
        addCentered(card, controlsAndResetSection(controlsValue, laneGrid(laneButtons), resetControls, resetProgress));
        card.add(Box.createVerticalGlue());
        addCentered(card, rightAlignedRow(backButton));

        root.add(card);

        installControlCapture(root, editingLane, refresh);
        return root;
    }

    private JSlider createVolumeSlider() {
        JSlider slider = new JSlider(0, 100, options.getMusicVolumePercent());
        slider.setOpaque(false);
        slider.setPreferredSize(SLIDER_SIZE);
        slider.setMinimumSize(SLIDER_SIZE);
        slider.setMaximumSize(SLIDER_SIZE);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(false);
        slider.setSnapToTicks(false);
        slider.setFocusable(false);
        slider.setUI(new VintageSliderUi(slider));
        return slider;
    }

    private JLabel vintageLabel(String text, Font font, Color color) {
        JLabel label = GameUiFactory.createLabel(text, color, font);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        return label;
    }

    private JButton createVintageButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Georgia", Font.BOLD, 15));
        button.setForeground(new Color(250, 234, 202));
        button.setBackground(WOOD);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WOOD_DARK, 2),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createControlButton() {
        JButton button = GameUiFactory.createImageStateButton(
                AssetCatalog.buttonStateUrl("Emp", "S"),
                AssetCatalog.buttonStateUrl("Emp", "H"),
                AssetCatalog.buttonStateUrl("Emp", "P"),
                "EMP",
                LANE_BUTTON_WIDTH
        );
        button.setFont(new Font("Georgia", Font.BOLD, 14));
        button.setForeground(new Color(238, 220, 185));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(0);
        button.setFocusable(false);
        setFixedSize(button, button.getPreferredSize());
        return button;
    }

    private void installControlCapture(JPanel root, int[] editingLane, Runnable refresh) {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed ESCAPE"), "cancelControlEdit");
        root.getActionMap().put("cancelControlEdit", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editingLane[0] = -1;
                refresh.run();
            }
        });

        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        KeyEventDispatcher dispatcher = event -> {
            if (editingLane[0] == -1 || event.getID() != KeyEvent.KEY_PRESSED || !root.isShowing()) {
                return false;
            }

            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.VK_ESCAPE) {
                editingLane[0] = -1;
            } else {
                options.setLaneKeyCode(editingLane[0], keyCode);
                editingLane[0] = -1;
            }

            refresh.run();
            return true;
        };

        keyboardFocusManager.addKeyEventDispatcher(dispatcher);
        root.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
                    && !root.isDisplayable()) {
                keyboardFocusManager.removeKeyEventDispatcher(dispatcher);
            }
        });
    }

    private JPanel row(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        for (JButton button : buttons) {
            row.add(button);
        }

        return row;
    }

    private JPanel laneGrid(JButton... buttons) {
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 8));
        grid.setOpaque(false);
        grid.setPreferredSize(LANE_GRID_SIZE);
        grid.setMinimumSize(LANE_GRID_SIZE);
        grid.setMaximumSize(LANE_GRID_SIZE);

        for (JButton button : buttons) {
            grid.add(button);
        }

        return grid;
    }

    private JPanel controlsAndResetSection(JLabel controlsValue, JPanel laneGrid, JButton resetControls, JButton resetProgress) {
        JPanel section = new JPanel(new BorderLayout(6, 0));
        section.setOpaque(false);
        Dimension size = new Dimension(SLIDER_SIZE.width, LANE_GRID_SIZE.height + 32);
        section.setPreferredSize(size);
        section.setMaximumSize(size);

        JPanel controlsColumn = new JPanel();
        controlsColumn.setOpaque(false);
        controlsColumn.setLayout(new BoxLayout(controlsColumn, BoxLayout.Y_AXIS));
        controlsValue.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        laneGrid.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        controlsColumn.add(controlsValue);
        controlsColumn.add(Box.createVerticalStrut(10));
        controlsColumn.add(laneGrid);

        JPanel resetColumn = new JPanel();
        resetColumn.setOpaque(false);
        resetColumn.setLayout(new BoxLayout(resetColumn, BoxLayout.Y_AXIS));
        resetColumn.setPreferredSize(new Dimension(RESET_COLUMN_WIDTH, size.height));
        resetColumn.setMaximumSize(new Dimension(RESET_COLUMN_WIDTH, size.height));
        resetColumn.add(Box.createVerticalStrut(34));
        resetColumn.add(rightAlignedButtonRow(resetControls, RESET_COLUMN_WIDTH));
        resetColumn.add(Box.createVerticalStrut(12));
        resetColumn.add(rightAlignedButtonRow(resetProgress, RESET_COLUMN_WIDTH));

        section.add(controlsColumn, BorderLayout.WEST);
        section.add(sectionSeparator(size.height), BorderLayout.CENTER);
        section.add(resetColumn, BorderLayout.EAST);
        return section;
    }

    private JComponent sectionSeparator(int height) {
        JComponent separator = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = getWidth() / 2;
                g.setColor(new Color(255, 241, 204, 90));
                g.drawLine(x - 1, 12, x - 1, getHeight() - 12);
                g.setColor(new Color(116, 82, 48, 145));
                g.drawLine(x, 10, x, getHeight() - 10);
                g.dispose();
            }
        };
        separator.setOpaque(false);
        Dimension size = new Dimension(2, Math.max(1, height));
        separator.setPreferredSize(size);
        separator.setMaximumSize(size);
        return separator;
    }

    private JPanel rightAlignedButtonRow(JButton button, int width) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setOpaque(false);
        Dimension size = new Dimension(width, Math.max(40, button.getPreferredSize().height));
        row.setPreferredSize(size);
        row.setMaximumSize(size);
        row.add(button);
        return row;
    }

    private JPanel justifiedRow(JButton... buttons) {
        JPanel row = new JPanel(new GridLayout(1, buttons.length, 28, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(SLIDER_SIZE.width, 44));
        row.setPreferredSize(new Dimension(SLIDER_SIZE.width, 44));

        for (JButton button : buttons) {
            JPanel cell = new JPanel(new GridBagLayout());
            cell.setOpaque(false);
            cell.add(button);
            row.add(cell);
        }

        return row;
    }

    private JPanel contentRow(JComponent component, String position) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        int height = Math.max(1, component.getPreferredSize().height);
        Dimension size = new Dimension(SLIDER_SIZE.width, height);
        row.setPreferredSize(size);
        row.setMaximumSize(size);
        row.add(component, position);
        return row;
    }

    private JPanel rightAlignedRow(JButton button) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(SLIDER_SIZE.width, 44));
        row.setPreferredSize(new Dimension(SLIDER_SIZE.width, 44));
        row.add(button);
        return row;
    }

    private void setFixedSize(JButton button, Dimension size) {
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }

    private void addLeft(JPanel panel, javax.swing.JComponent component) {
        component.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(component);
    }

    private void addCentered(JPanel panel, JComponent component) {
        component.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(component);
    }

    private static final class ParchmentCardPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private ParchmentCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = 20;

            g.setColor(new Color(0, 0, 0, 82));
            g.fill(new RoundRectangle2D.Double(8, 10, width - 16, height - 14, arc, arc));

            g.setPaint(new GradientPaint(0, 0, PARCHMENT, 0, height, new Color(202, 165, 112)));
            g.fill(new RoundRectangle2D.Double(0, 0, width - 10, height - 12, arc, arc));

            g.setColor(new Color(122, 74, 35, 76));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(8, 8, width - 26, height - 28, arc - 4, arc - 4));

            g.setColor(new Color(102, 66, 34, 34));
            g.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 4; i++) {
                int radius = 58 + i * 22;
                g.draw(new Ellipse2D.Double(width - 172 - i * 8, 58 - i * 8, radius, radius));
            }

            g.dispose();
        }
    }

    private static final class OptionsBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final AnimatedGifBackground backgroundImage;
        private final javax.swing.Timer animationTimer;

        private OptionsBackgroundPanel(AnimatedGifBackground backgroundImage) {
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
            g.setPaint(new GradientPaint(0, 0, ROOT_TOP, 0, getHeight(), ROOT_BOTTOM));
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

    private static final class VintageSliderUi extends BasicSliderUI {
        private static final int TRACK_HEIGHT = 12;
        private static final int TICK_HEIGHT = 6;
        private static final int THUMB_SIZE = 28;

        private VintageSliderUi(JSlider slider) {
            super(slider);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }

        @Override
        public void paintTrack(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = trackRect.x;
            int y = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2;
            int width = trackRect.width;
            int fillWidth = Math.max(0, thumbRect.x + (thumbRect.width / 2) - x);

            g.setColor(new Color(73, 43, 22, 55));
            g.fillRoundRect(x + 1, y + 4, width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

            g.setColor(new Color(238, 221, 184));
            g.fillRoundRect(x, y, width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

            g.setPaint(new GradientPaint(x, y, new Color(142, 78, 34), x, y + TRACK_HEIGHT, new Color(90, 49, 24)));
            g.fillRoundRect(x, y, fillWidth, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

            g.setColor(new Color(89, 50, 25, 118));
            g.setStroke(new BasicStroke(1.1f));
            g.drawRoundRect(x, y, width - 1, TRACK_HEIGHT - 1, TRACK_HEIGHT, TRACK_HEIGHT);

            paintTinyTicks(g, x, y + TRACK_HEIGHT + 10, width);
            g.dispose();
        }

        private void paintTinyTicks(Graphics2D g, int x, int y, int width) {
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int value = slider.getMinimum(); value <= slider.getMaximum(); value += 10) {
                int tickX = xPositionForValue(value);
                int tickHeight = value % 25 == 0 ? TICK_HEIGHT + 3 : TICK_HEIGHT;
                g.setColor(value <= slider.getValue() ? new Color(111, 63, 31, 185) : new Color(122, 87, 54, 112));
                g.drawLine(tickX, y, tickX, y + tickHeight);
            }

            g.setFont(new Font("Georgia", Font.BOLD, 10));
            g.setColor(new Color(99, 65, 38, 130));
            FontMetrics metrics = g.getFontMetrics();
            String low = "0";
            String high = "100";
            g.drawString(low, x - metrics.stringWidth(low) / 2, y + 23);
            g.drawString(high, x + width - metrics.stringWidth(high) / 2, y + 23);
        }

        @Override
        public void paintThumb(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = thumbRect.x;
            int y = thumbRect.y;
            int size = thumbRect.width;

            g.setColor(new Color(61, 35, 18, 72));
            g.fillOval(x + 3, y + 5, size, size);

            g.setPaint(new GradientPaint(x, y, new Color(255, 247, 221), x, y + size, new Color(216, 184, 130)));
            g.fillOval(x, y, size, size);

            g.setColor(new Color(111, 67, 32));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x + 1, y + 1, size - 3, size - 3);

            g.setColor(new Color(255, 255, 245, 150));
            g.fillOval(x + 8, y + 6, 8, 6);
            g.dispose();
        }

        @Override
        protected void calculateTrackRect() {
            super.calculateTrackRect();
            trackRect = new Rectangle(trackRect.x + 4, trackRect.y - 8, trackRect.width - 8, 34);
        }
    }
}
