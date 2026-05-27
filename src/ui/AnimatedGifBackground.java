package ui;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AnimatedGifBackground {
    private static final int DEFAULT_FRAME_MILLIS = 100;
    private static final int MIN_FRAME_MILLIS = 20;

    private final List<BufferedImage> frames;
    private final int[] frameDurations;
    private final int totalDurationMillis;
    private final long startedAtMillis;

    private AnimatedGifBackground(List<BufferedImage> frames, int[] frameDurations, int totalDurationMillis) {
        this.frames = frames;
        this.frameDurations = frameDurations;
        this.totalDurationMillis = totalDurationMillis;
        this.startedAtMillis = System.currentTimeMillis();
    }

    public static AnimatedGifBackground load(URL url) {
        if (url == null) {
            return null;
        }

        try (InputStream input = url.openStream(); ImageInputStream stream = ImageIO.createImageInputStream(input)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, false, false);
                return readFrames(reader);
            } finally {
                reader.dispose();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public static AnimatedGifBackground load(URL[] urls) {
        if (urls == null || urls.length == 0) {
            return null;
        }

        if (urls.length == 1) {
            return load(urls[0]);
        }

        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();

        for (URL url : urls) {
            if (url == null) {
                continue;
            }

            try {
                BufferedImage image = ImageIO.read(url);
                if (image != null) {
                    frames.add(image);
                    durations.add(0);
                }
            } catch (Exception ignored) {
            }
        }

        if (frames.isEmpty()) {
            return null;
        }

        int[] frameDurations = new int[durations.size()];
        int totalDuration = 0;
        for (int i = 0; i < durations.size(); i++) {
            frameDurations[i] = durations.get(i);
            totalDuration += frameDurations[i];
        }

        return new AnimatedGifBackground(frames, frameDurations, totalDuration);
    }

    public BufferedImage currentFrame() {
        if (frames.isEmpty()) {
            return null;
        }

        if (frames.size() == 1 || totalDurationMillis <= 0) {
            return frames.get(0);
        }

        int elapsed = (int) ((System.currentTimeMillis() - startedAtMillis) % totalDurationMillis);
        int frameTime = 0;

        for (int i = 0; i < frameDurations.length; i++) {
            frameTime += frameDurations[i];
            if (elapsed < frameTime) {
                return frames.get(i);
            }
        }

        return frames.get(frames.size() - 1);
    }

    public BufferedImage frameAt(int frameIndex) {
        if (frames.isEmpty()) {
            return null;
        }

        int boundedIndex = Math.max(0, Math.min(frameIndex, frames.size() - 1));
        return frames.get(boundedIndex);
    }

    public int frameCount() {
        return frames.size();
    }

    public boolean isAnimated() {
        return frames.size() > 1 && totalDurationMillis > 0;
    }

    private static AnimatedGifBackground readFrames(ImageReader reader) throws Exception {
        int frameCount = reader.getNumImages(true);
        if (frameCount <= 0) {
            return null;
        }

        Dimension logicalSize = logicalScreenSize(reader);
        if (logicalSize.width <= 0 || logicalSize.height <= 0) {
            BufferedImage firstFrame = reader.read(0);
            logicalSize = new Dimension(firstFrame.getWidth(), firstFrame.getHeight());
        }

        List<BufferedImage> frames = new ArrayList<>();
        int[] durations = new int[frameCount];
        BufferedImage canvas = new BufferedImage(logicalSize.width, logicalSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvasGraphics = canvas.createGraphics();

        try {
            for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                FrameMetadata metadata = frameMetadata(reader.getImageMetadata(frameIndex));
                BufferedImage beforeFrame = metadata.restoreToPrevious()
                        ? deepCopy(canvas)
                        : null;
                BufferedImage frame = reader.read(frameIndex);

                canvasGraphics.drawImage(frame, metadata.x(), metadata.y(), null);
                frames.add(deepCopy(canvas));
                durations[frameIndex] = metadata.durationMillis();

                if (metadata.restoreToBackground()) {
                    canvasGraphics.setComposite(AlphaComposite.Clear);
                    canvasGraphics.fillRect(metadata.x(), metadata.y(), frame.getWidth(), frame.getHeight());
                    canvasGraphics.setComposite(AlphaComposite.SrcOver);
                } else if (beforeFrame != null) {
                    canvasGraphics.dispose();
                    canvas = deepCopy(beforeFrame);
                    canvasGraphics = canvas.createGraphics();
                }
            }
        } finally {
            canvasGraphics.dispose();
        }

        int totalDuration = 0;
        for (int duration : durations) {
            totalDuration += duration;
        }

        return new AnimatedGifBackground(frames, durations, totalDuration);
    }

    private static Dimension logicalScreenSize(ImageReader reader) throws Exception {
        IIOMetadata metadata = reader.getStreamMetadata();
        if (metadata == null || metadata.getNativeMetadataFormatName() == null) {
            return new Dimension(0, 0);
        }

        Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node descriptor = findNode(root, "LogicalScreenDescriptor");
        if (descriptor == null) {
            return new Dimension(0, 0);
        }

        return new Dimension(
                intAttribute(descriptor, "logicalScreenWidth", 0),
                intAttribute(descriptor, "logicalScreenHeight", 0)
        );
    }

    private static FrameMetadata frameMetadata(IIOMetadata metadata) throws Exception {
        Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node control = findNode(root, "GraphicControlExtension");
        Node descriptor = findNode(root, "ImageDescriptor");

        int delay = DEFAULT_FRAME_MILLIS;
        String disposal = "none";
        if (control != null) {
            delay = Math.max(MIN_FRAME_MILLIS, intAttribute(control, "delayTime", DEFAULT_FRAME_MILLIS / 10) * 10);
            disposal = stringAttribute(control, "disposalMethod", "none");
        }

        int x = descriptor == null ? 0 : intAttribute(descriptor, "imageLeftPosition", 0);
        int y = descriptor == null ? 0 : intAttribute(descriptor, "imageTopPosition", 0);
        return new FrameMetadata(x, y, delay, disposal);
    }

    private static Node findNode(Node node, String name) {
        if (node == null) {
            return null;
        }

        if (name.equals(node.getNodeName())) {
            return node;
        }

        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = findNode(child, name);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private static int intAttribute(Node node, String name, int fallback) {
        String value = stringAttribute(node, name, null);
        if (value == null) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String stringAttribute(Node node, String name, String fallback) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null || attributes.getNamedItem(name) == null) {
            return fallback;
        }

        return attributes.getNamedItem(name).getNodeValue();
    }

    private static BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    private record FrameMetadata(int x, int y, int durationMillis, String disposalMethod) {
        private boolean restoreToBackground() {
            return "restoreToBackgroundColor".equals(disposalMethod);
        }

        private boolean restoreToPrevious() {
            return "restoreToPrevious".equals(disposalMethod);
        }
    }
}
