package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class GameUiFactory {
    private static final Font MENU_FONT = new Font("Georgia", Font.BOLD, 28);
    private static final Font SMALL_FONT = new Font("Georgia", Font.BOLD, 16);

    private GameUiFactory() {
    }

    public static JButton createMenuButton(String text) {
        JButton button = createTransparentButton(text, MENU_FONT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        addHover(button, Color.WHITE, Color.ORANGE);
        return button;
    }

    public static JButton createGameBoard(String text) {
        return createMenuButton(text);
    }

    public static JButton createStageBoard(String text, boolean unlocked) {
        JButton button = createTransparentButton(text, new Font("Georgia", Font.BOLD, 24));
        button.setPreferredSize(new Dimension(500, 65));
        button.setMaximumSize(new Dimension(500, 65));
        button.setMinimumSize(new Dimension(500, 65));
        button.setHorizontalAlignment(SwingConstants.LEFT);

        if (unlocked) {
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));
            addHover(button, Color.WHITE, Color.ORANGE);
        } else {
            button.setForeground(Color.GRAY);
            button.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(85, 85, 85)));
            addHover(button, Color.GRAY, Color.ORANGE);
        }

        return button;
    }

    public static JButton createSmallButton(String text) {
        JButton button = createTransparentButton(text, SMALL_FONT);
        button.setPreferredSize(new Dimension(120, 40));
        button.setMinimumSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));
        addHover(button, Color.WHITE, Color.ORANGE);
        return button;
    }

    public static JLabel createOptionValueLabel() {
        JLabel label = new JLabel();
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        return label;
    }

    public static JLabel createResultLabel(String value) {
        JLabel label = new JLabel(value);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }

    public static JLabel createLabel(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    public static JTextArea createWrappedText(String text, Color color, Font font, int width) {
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

    private static JButton createTransparentButton(String text, Font font) {
        JButton button = new JButton(underline(text));
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        return button;
    }

    private static void addHover(JButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setForeground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setForeground(normal);
                }
            }
        });
    }

    private static String underline(String text) {
        return "<html><u>" + escapeHtml(text) + "</u></html>";
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
