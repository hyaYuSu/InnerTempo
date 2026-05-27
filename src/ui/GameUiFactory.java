package ui;

import config.AssetCatalog;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

public final class GameUiFactory {
    private static final Font MENU_FONT = new Font("Georgia", Font.BOLD, 28);
    private static final Font SMALL_FONT = new Font("Georgia", Font.BOLD, 16);
    private static final int BUTTON_CROP_PADDING = 0;
    private static final int SMALL_IMAGE_BUTTON_HEIGHT = 40;

    private GameUiFactory() {
    }

    public static JButton createMenuButton(String text) {
        JButton button = createTransparentButton(text, MENU_FONT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        addHover(button, Color.WHITE, Color.ORANGE);
        return button;
    }

    public static JButton createImageStateButton(
            URL staticUrl,
            URL hoverUrl,
            URL pressedUrl,
            String fallbackText,
            int targetWidth
    ) {
        BufferedImage staticImage = loadImage(staticUrl);
        BufferedImage hoverImage = loadImage(hoverUrl);
        BufferedImage pressedImage = loadImage(pressedUrl);

        if (staticImage == null || hoverImage == null || pressedImage == null) {
            return createMenuButton(fallbackText);
        }

        Rectangle cropBounds = alphaBounds(staticImage);
        cropBounds = cropBounds.union(alphaBounds(hoverImage));
        cropBounds = cropBounds.union(alphaBounds(pressedImage));
        cropBounds = paddedBounds(cropBounds, staticImage.getWidth(), staticImage.getHeight());

        ImageIcon staticIcon = croppedScaledIcon(staticImage, cropBounds, targetWidth);
        ImageIcon hoverIcon = croppedScaledIcon(hoverImage, cropBounds, targetWidth);
        ImageIcon pressedIcon = croppedScaledIcon(pressedImage, cropBounds, targetWidth);

        JButton button = new JButton(staticIcon);
        button.setRolloverIcon(hoverIcon);
        button.setPressedIcon(pressedIcon);
        button.setDisabledIcon(staticIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));

        Dimension size = new Dimension(staticIcon.getIconWidth(), staticIcon.getIconHeight());
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
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
        JButton imageButton = createKnownImageButton(text, 132);
        if (imageButton != null) {
            return imageButton;
        }

        JButton button = createTransparentButton(text, SMALL_FONT);
        button.setPreferredSize(new Dimension(120, 40));
        button.setMinimumSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));
        addHover(button, Color.WHITE, Color.ORANGE);
        return button;
    }

    public static JButton createFixedSmallButton(String text, Dimension size) {
        JButton imageButton = createFixedKnownImageButton(text, size);
        if (imageButton != null) {
            return imageButton;
        }

        JButton button = createTransparentButton(text, SMALL_FONT);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
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

    private static JButton createKnownImageButton(String text, int targetWidth) {
        String buttonId = buttonAssetId(text);
        if (buttonId == null) {
            return null;
        }

        int resolvedTargetWidth = imageButtonWidth(text, targetWidth);
        JButton button = createImageStateButton(
                AssetCatalog.buttonStateUrl(buttonId, "S"),
                AssetCatalog.buttonStateUrl(buttonId, "H"),
                AssetCatalog.buttonStateUrl(buttonId, "P"),
                text,
                resolvedTargetWidth
        );

        return button.getIcon() instanceof ImageIcon ? button : null;
    }

    private static JButton createFixedKnownImageButton(String text, Dimension size) {
        String buttonId = buttonAssetId(text);
        if (buttonId == null) {
            return null;
        }

        JButton button = createFixedImageStateButton(
                AssetCatalog.buttonStateUrl(buttonId, "S"),
                AssetCatalog.buttonStateUrl(buttonId, "H"),
                AssetCatalog.buttonStateUrl(buttonId, "P"),
                text,
                size
        );

        return button.getIcon() instanceof ImageIcon ? button : null;
    }

    private static JButton createFixedImageStateButton(
            URL staticUrl,
            URL hoverUrl,
            URL pressedUrl,
            String fallbackText,
            Dimension size
    ) {
        BufferedImage staticImage = loadImage(staticUrl);
        BufferedImage hoverImage = loadImage(hoverUrl);
        BufferedImage pressedImage = loadImage(pressedUrl);

        if (staticImage == null || hoverImage == null || pressedImage == null) {
            return createMenuButton(fallbackText);
        }

        Rectangle cropBounds = alphaBounds(staticImage);
        cropBounds = cropBounds.union(alphaBounds(hoverImage));
        cropBounds = cropBounds.union(alphaBounds(pressedImage));
        cropBounds = paddedBounds(cropBounds, staticImage.getWidth(), staticImage.getHeight());

        ImageIcon staticIcon = croppedScaledIcon(staticImage, cropBounds, size);
        ImageIcon hoverIcon = croppedScaledIcon(hoverImage, cropBounds, size);
        ImageIcon pressedIcon = croppedScaledIcon(pressedImage, cropBounds, size);

        JButton button = new JButton(staticIcon);
        button.setRolloverIcon(hoverIcon);
        button.setPressedIcon(pressedIcon);
        button.setDisabledIcon(staticIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        return button;
    }

    private static int imageButtonWidth(String text, int fallbackWidth) {
        return switch (text) {
            case "RESET PROGRESS" -> 180;
            case "RESUME" -> 145;
            default -> fallbackWidth;
        };
    }

    private static String buttonAssetId(String text) {
        return switch (text) {
            case "BACK" -> "Back";
            case "HOME", "SCENES" -> "Home";
            case "NEXT" -> "Next";
            case "OPTIONS" -> "Opt";
            case "PAUSE" -> "Pause";
            case "PLAY" -> "Ply";
            case "EMP" -> "Emp";
            case "RESET" -> "Reset";
            case "RESET PROGRESS" -> "ResetP";
            case "RESUME" -> "Rsme";
            case "RETRY" -> "Rtry";
            default -> null;
        };
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

    private static BufferedImage loadImage(URL imageUrl) {
        if (imageUrl == null) {
            return null;
        }

        try {
            return ImageIO.read(imageUrl);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Rectangle alphaBounds(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >>> 24) & 0xff;
                if (alpha > 8) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new Rectangle(0, 0, image.getWidth(), image.getHeight());
        }

        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static Rectangle paddedBounds(Rectangle bounds, int imageWidth, int imageHeight) {
        int x = Math.max(0, bounds.x - BUTTON_CROP_PADDING);
        int y = Math.max(0, bounds.y - BUTTON_CROP_PADDING);
        int right = Math.min(imageWidth, bounds.x + bounds.width + BUTTON_CROP_PADDING);
        int bottom = Math.min(imageHeight, bounds.y + bounds.height + BUTTON_CROP_PADDING);
        return new Rectangle(x, y, right - x, bottom - y);
    }

    private static ImageIcon croppedScaledIcon(BufferedImage image, Rectangle cropBounds, int targetWidth) {
        BufferedImage cropped = image.getSubimage(cropBounds.x, cropBounds.y, cropBounds.width, cropBounds.height);
        int height = targetWidth <= 180 ? SMALL_IMAGE_BUTTON_HEIGHT : Math.max(1, Math.round(cropBounds.height * (targetWidth / (float) cropBounds.width)));
        int width = targetWidth <= 180 ? Math.max(1, Math.round(cropBounds.width * (height / (float) cropBounds.height))) : targetWidth;
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(cropped.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, width, height, null);
        g.dispose();
        return new ImageIcon(scaled);
    }

    private static ImageIcon croppedScaledIcon(BufferedImage image, Rectangle cropBounds, Dimension size) {
        BufferedImage cropped = image.getSubimage(cropBounds.x, cropBounds.y, cropBounds.width, cropBounds.height);
        BufferedImage scaled = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(cropped.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH), 0, 0, size.width, size.height, null);
        g.dispose();
        return new ImageIcon(scaled);
    }
}
