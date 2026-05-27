package screens;

import config.AssetCatalog;
import config.GameConfig;
import ui.GameUiFactory;
import ui.MenuCardLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class PauseMenu {
    private static final int BUTTON_WIDTH = MenuCardLayout.MAIN_MENU_BUTTON_WIDTH - 8;

    private final Runnable onTogglePause;
    private final Runnable onResume;
    private final Runnable onRetry;
    private final Runnable onScenes;
    private final Runnable onOptions;
    private final List<JButton> menuButtons = new ArrayList<>();

    private JButton pauseButton;

    public PauseMenu(
            Runnable onTogglePause,
            Runnable onResume,
            Runnable onRetry,
            Runnable onScenes,
            Runnable onOptions
    ) {
        this.onTogglePause = onTogglePause;
        this.onResume = onResume;
        this.onRetry = onRetry;
        this.onScenes = onScenes;
        this.onOptions = onOptions;
    }

    public void install(JPanel root) {
        pauseButton = GameUiFactory.createSmallButton("PAUSE");
        placeButton(pauseButton, 30, 25);
        pauseButton.addActionListener(e -> onTogglePause.run());
        root.add(pauseButton);

        JButton resumeButton = createMenuButton("RESUME", "Rsme", onResume);
        JButton retryButton = createMenuButton("RETRY", "Rtry", onRetry);
        JButton scenesButton = createMenuButton("SCENES", "Home", onScenes);
        JButton optionsButton = createMenuButton("OPTIONS", "Opt", onOptions);

        JButton[] buttons = {resumeButton, retryButton, scenesButton, optionsButton};
        int y = 238;
        for (JButton button : buttons) {
            Dimension size = button.getPreferredSize();
            int buttonX = (GameConfig.SCENE_WIDTH - size.width) / 2;
            button.setBounds(buttonX, y, size.width, size.height);
            button.setVisible(false);
            menuButtons.add(button);
            root.add(button);
            y += size.height + 7;
        }
    }

    public void setMenuVisible(boolean visible) {
        if (pauseButton != null) {
            pauseButton.setVisible(!visible);
        }

        for (JButton button : menuButtons) {
            button.setVisible(visible);
        }
    }

    public void hidePauseButton() {
        if (pauseButton != null) {
            pauseButton.setVisible(false);
        }
    }

    private JButton createMenuButton(String text, String assetId, Runnable action) {
        JButton button = GameUiFactory.createImageStateButton(
                AssetCatalog.buttonStateUrl(assetId, "S"),
                AssetCatalog.buttonStateUrl(assetId, "H"),
                AssetCatalog.buttonStateUrl(assetId, "P"),
                text,
                BUTTON_WIDTH
        );
        button.addActionListener(e -> action.run());
        return button;
    }

    private void placeButton(JButton button, int x, int y) {
        Dimension size = button.getPreferredSize();
        button.setBounds(x, y, size.width, size.height);
    }
}
