package screens;

import config.AssetCatalog;
import manager.ScreenManager;
import ui.WarmCinematicImagePanel;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;

public class TitleScreen {
    private final ScreenManager controller;

    public TitleScreen(ScreenManager controller) {
        this.controller = controller;
    }

    public JPanel create() {
        JPanel root = new WarmCinematicImagePanel(loadTitleAsset());
        root.setLayout(new BorderLayout());
        root.setFocusable(true);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(40, 40, 0, 0));

        JLabel title = new JLabel("INNER TEMPO");
        title.setForeground(new Color(74, 52, 34));
        title.setFont(new Font("Georgia", Font.BOLD, 60));
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JLabel subTitle = new JLabel("Find your rhythm.");
        subTitle.setFont(new Font("Serif", Font.ITALIC, 20));
        subTitle.setForeground(new Color(216, 193, 106));
        subTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        center.add(title);
        center.add(subTitle);

        JLabel pressText = new JLabel("Press SPACE to start...");
        pressText.setForeground(new Color(229, 184, 96));
        pressText.setFont(new Font("Georgia", Font.BOLD, 18));
        pressText.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));

        root.add(center, BorderLayout.CENTER);
        root.add(pressText, BorderLayout.SOUTH);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "showMainMenu");
        root.getActionMap().put("showMainMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.showMainMenu();
            }
        });

        return root;
    }

    private ImageIcon loadTitleAsset() {
        URL titleUrl = AssetCatalog.titleScreenUrl();
        if (titleUrl == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(titleUrl);
        return icon.getIconWidth() > 0 ? icon : null;
    }
}
