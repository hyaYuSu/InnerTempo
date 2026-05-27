package screens;

import config.AssetCatalog;
import manager.ScreenManager;
import model.Journey;
import model.JourneyScene;
import ui.AnimatedGifBackground;
import ui.GameUiFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class JourneyScenePlaceholderScreen {
    private final ScreenManager controller;
    private final Journey journey;
    private final JourneyScene scene;

    public JourneyScenePlaceholderScreen(ScreenManager controller, Journey journey, JourneyScene scene) {
        this.controller = controller;
        this.journey = journey;
        this.scene = scene;
    }

    public JPanel create() {
        JPanel root = new SceneImageBackgroundPanel(
                backgroundTop(),
                backgroundBottom(),
                AnimatedGifBackground.load(AssetCatalog.backgroundUrlFor(scene.getPlayableStage()))
        );
        root.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        JLabel journeyTitle = GameUiFactory.createLabel(
                journey.getTitle(),
                new Color(218, 165, 32),
                new Font("Georgia", Font.BOLD, 42)
        );
        JLabel sceneTitle = GameUiFactory.createLabel(
                "Scene " + scene.getNumber() + ": " + scene.getTitle(),
                Color.WHITE,
                new Font("Georgia", Font.BOLD, 30)
        );
        JTextArea background = GameUiFactory.createWrappedText(
                journey.getBackgroundLabel(),
                Color.LIGHT_GRAY,
                new Font("Georgia", Font.PLAIN, 20),
                760
        );
        JTextArea story = GameUiFactory.createWrappedText(
                scene.getStoryText().isBlank() ? scene.getSummary() : scene.getStoryText(),
                Color.WHITE,
                new Font("Georgia", Font.PLAIN, 19),
                780
        );

        JButton backButton = GameUiFactory.createSmallButton("BACK");
        backButton.addActionListener(e -> controller.showJourneySelect(journey.getId()));

        addLeft(content, journeyTitle);
        content.add(Box.createVerticalStrut(22));
        addLeft(content, sceneTitle);
        content.add(Box.createVerticalStrut(22));
        addLeft(content, background);
        content.add(Box.createVerticalStrut(22));
        addLeft(content, story);
        content.add(Box.createVerticalStrut(22));
        addLeft(content, backButton);

        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private Color backgroundTop() {
        return switch (scene.getNumber()) {
            case 1 -> new Color(75, 53, 39);
            case 2 -> new Color(43, 37, 38);
            case 3 -> new Color(37, 53, 72);
            case 4 -> new Color(70, 56, 32);
            default -> new Color(93, 58, 47);
        };
    }

    private Color backgroundBottom() {
        return switch (scene.getNumber()) {
            case 1 -> new Color(18, 13, 10);
            case 2 -> new Color(8, 7, 7);
            case 3 -> new Color(9, 13, 18);
            case 4 -> new Color(15, 11, 8);
            default -> new Color(27, 16, 21);
        };
    }

    private void addLeft(JPanel panel, javax.swing.JComponent component) {
        component.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(component);
    }

    private static final class SceneImageBackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private final Color topColor;
        private final Color bottomColor;
        private final AnimatedGifBackground backgroundImage;
        private final Timer animationTimer;

        private SceneImageBackgroundPanel(
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
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
            g.fillRect(0, 0, getWidth(), getHeight());

            BufferedImage frame = backgroundImage == null ? null : backgroundImage.currentFrame();
            if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
                drawCoverImage(g, frame);
                g.setColor(new Color(0, 0, 0, 132));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 18), 0, getHeight(), new Color(0, 0, 0, 190)));
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
}
