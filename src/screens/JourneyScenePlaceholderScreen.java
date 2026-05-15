package screens;

import manager.ScreenManager;
import model.Journey;
import model.JourneyScene;
import ui.GameUiFactory;
import ui.GradientPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

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
        JPanel root = new GradientPanel(backgroundTop(), backgroundBottom());
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
}
