package screens;

import config.AssetCatalog;
import manager.ScreenManager;
import ui.GameUiFactory;
import ui.MenuCardLayout;
import ui.WarmCinematicImagePanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

public class MainMenuScreen {
    private final ScreenManager controller;

    public MainMenuScreen(ScreenManager controller) {
        this.controller = controller;
    }

    public JPanel create() {
        JPanel root = new WarmCinematicImagePanel(loadMenuAsset());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(60, 60, 0, 0));

        JLabel title = new JLabel("INNER TEMPO");
        title.setForeground(new Color(74, 52, 34));
        title.setFont(new Font("Georgia", Font.BOLD, 58));
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JButton playButton = createImageMenuButton(
                AssetCatalog.playButtonStaticUrl(),
                AssetCatalog.playButtonHoverUrl(),
                AssetCatalog.playButtonPressedUrl(),
                "PLAY"
        );
        JButton optionsButton = createImageMenuButton(
                AssetCatalog.optionsButtonStaticUrl(),
                AssetCatalog.optionsButtonHoverUrl(),
                AssetCatalog.optionsButtonPressedUrl(),
                "OPTIONS"
        );
        playButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        optionsButton.setAlignmentX(JButton.LEFT_ALIGNMENT);

        playButton.addActionListener(e -> controller.showJourneySelect());
        optionsButton.addActionListener(e -> controller.showOptions());

        root.add(title);
        root.add(Box.createVerticalStrut(2));
        root.add(playButton);
        root.add(Box.createVerticalStrut(0));
        root.add(optionsButton);

        return root;
    }

    private JButton createImageMenuButton(URL staticUrl, URL hoverUrl, URL pressedUrl, String fallbackText) {
        return GameUiFactory.createImageStateButton(
                staticUrl,
                hoverUrl,
                pressedUrl,
                fallbackText,
                MenuCardLayout.MAIN_MENU_BUTTON_WIDTH
        );
    }

    private ImageIcon loadMenuAsset() {
        URL imageUrl = AssetCatalog.titleScreenUrl();
        if (imageUrl == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(imageUrl);
        return icon.getIconWidth() > 0 ? icon : null;
    }
}
