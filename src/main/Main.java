package main;

import manager.ScreenManager;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inner Tempo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            ScreenManager screenManager = new ScreenManager(frame);
            screenManager.showTitle();

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
