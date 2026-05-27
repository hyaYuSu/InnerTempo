package ui;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Dimension;

public final class MenuCardLayout {
    public static final int MAIN_MENU_BUTTON_WIDTH = 190;
    public static final int JOURNEY_MENU_WIDTH = 270;
    public static final int SCENE_CARD_WIDTH = 192;
    public static final int SCENE_CARD_HEIGHT = 98;
    public static final int SCENE_GRID_COLUMNS = 3;
    public static final int SCENE_GRID_HORIZONTAL_GAP = 14;
    public static final int SCENE_GRID_VERTICAL_GAP = 12;
    public static final int DETAIL_PANEL_WIDTH = 820;
    public static final int DETAIL_PANEL_HEIGHT = 140;

    private MenuCardLayout() {
    }

    public static Border journeyMenuBorder() {
        return BorderFactory.createEmptyBorder(50, 42, 50, 28);
    }

    public static Border journeyDetailsBorder() {
        return BorderFactory.createEmptyBorder(34, 36, 28, 56);
    }

    public static Dimension journeyMenuSize() {
        return new Dimension(JOURNEY_MENU_WIDTH, 600);
    }

    public static Dimension sceneCardSize() {
        return new Dimension(SCENE_CARD_WIDTH, SCENE_CARD_HEIGHT);
    }

    public static Dimension sceneGridSize() {
        int width = (SCENE_CARD_WIDTH * SCENE_GRID_COLUMNS)
                + (SCENE_GRID_HORIZONTAL_GAP * (SCENE_GRID_COLUMNS - 1));
        int rows = 2;
        int height = (SCENE_CARD_HEIGHT * rows) + SCENE_GRID_VERTICAL_GAP;
        return new Dimension(width, height);
    }

    public static Dimension detailPanelSize() {
        return new Dimension(DETAIL_PANEL_WIDTH, DETAIL_PANEL_HEIGHT);
    }
}
