package screens;

import manager.ScreenManager;
import settings.GameplaySettings;
import ui.GameUiFactory;
import ui.GradientPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

public class OptionsScreen {
    private final ScreenManager controller;
    private final GameplaySettings options;

    public OptionsScreen(ScreenManager controller, GameplaySettings options) {
        this.controller = controller;
        this.options = options;
    }

    public JPanel create() {
        JPanel root = new GradientPanel(new Color(5, 5, 5));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(60, 60, 0, 0));

        JLabel title = GameUiFactory.createLabel(
                "OPTIONS",
                new Color(218, 165, 32),
                new Font("Georgia", Font.BOLD, 42)
        );
        JLabel speedValue = GameUiFactory.createOptionValueLabel();
        JLabel timingValue = GameUiFactory.createOptionValueLabel();
        JLabel offsetValue = GameUiFactory.createOptionValueLabel();

        Runnable refresh = () -> {
            speedValue.setText(String.format("Note Speed: %.1fx", options.getNoteSpeedMultiplier()));
            timingValue.setText("Timing: " + options.timingLabel());
            offsetValue.setText(String.format("Input Offset: %+d ms", Math.round(options.getInputOffsetSeconds() * 1000)));
        };

        JButton speedDown = GameUiFactory.createSmallButton("-");
        JButton speedUp = GameUiFactory.createSmallButton("+");
        speedDown.addActionListener(e -> {
            options.decreaseNoteSpeed();
            refresh.run();
        });
        speedUp.addActionListener(e -> {
            options.increaseNoteSpeed();
            refresh.run();
        });

        JButton strictButton = GameUiFactory.createSmallButton("STRICT");
        JButton normalButton = GameUiFactory.createSmallButton("NORMAL");
        JButton relaxedButton = GameUiFactory.createSmallButton("RELAXED");
        strictButton.addActionListener(e -> {
            options.useStrictTiming();
            refresh.run();
        });
        normalButton.addActionListener(e -> {
            options.useNormalTiming();
            refresh.run();
        });
        relaxedButton.addActionListener(e -> {
            options.useRelaxedTiming();
            refresh.run();
        });

        JButton offsetDown = GameUiFactory.createSmallButton("-10");
        JButton offsetReset = GameUiFactory.createSmallButton("RESET");
        JButton offsetUp = GameUiFactory.createSmallButton("+10");
        offsetDown.addActionListener(e -> {
            options.adjustInputOffsetMillis(-10);
            refresh.run();
        });
        offsetReset.addActionListener(e -> {
            options.resetInputOffset();
            refresh.run();
        });
        offsetUp.addActionListener(e -> {
            options.adjustInputOffsetMillis(10);
            refresh.run();
        });

        JButton backButton = GameUiFactory.createSmallButton("BACK");
        backButton.addActionListener(e -> controller.showMainMenu());

        refresh.run();

        addLeft(root, title);
        root.add(Box.createVerticalStrut(26));
        addLeft(root, speedValue);
        root.add(Box.createVerticalStrut(26));
        addLeft(root, row(speedDown, speedUp));
        root.add(Box.createVerticalStrut(26));
        addLeft(root, timingValue);
        root.add(Box.createVerticalStrut(26));
        addLeft(root, row(strictButton, normalButton, relaxedButton));
        root.add(Box.createVerticalStrut(26));
        addLeft(root, offsetValue);
        root.add(Box.createVerticalStrut(26));
        addLeft(root, row(offsetDown, offsetReset, offsetUp));
        root.add(Box.createVerticalStrut(26));
        addLeft(root, backButton);

        return root;
    }

    private JPanel row(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);

        for (JButton button : buttons) {
            row.add(button);
        }

        return row;
    }

    private void addLeft(JPanel panel, javax.swing.JComponent component) {
        component.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(component);
    }
}
