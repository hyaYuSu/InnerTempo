package config;

import model.Stages.PlayableStage;
import model.Stages.LittleBellStage;
import model.Stages.WaveStage;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AssetCatalog {

    // Images
    public static final String TITLE_SCREEN = "assets/images/TtileScreen.gif";
    public static final String PLAY_BUTTON_STATIC = "assets/images/buttons/Ply_S.png";
    public static final String PLAY_BUTTON_HOVER = "assets/images/buttons/Ply_H.png";
    public static final String PLAY_BUTTON_PRESSED = "assets/images/buttons/Ply_P.png";
    public static final String OPTIONS_BUTTON_STATIC = "assets/images/buttons/Opt_S.png";
    public static final String OPTIONS_BUTTON_HOVER = "assets/images/buttons/Opt_H.png";
    public static final String OPTIONS_BUTTON_PRESSED = "assets/images/buttons/Opt_P.png";
    public static final String WAVES_BACKGROUND = "assets/images/Waves1.gif";
    public static final String WAVES_SCENE_1 = "assets/images/waves/scene1.gif";
    public static final String WAVES_SCENE_2 = "assets/images/waves/scene2.gif";
    public static final String WAVES_SCENE_3 = "assets/images/waves/scene3.gif";
    public static final String WAVES_SCENE_4 = "assets/images/waves/scene4.gif";
    public static final String WAVES_SCENE_5 = "assets/images/waves/scene5.gif";
    public static final String WAVES_SCENE_6 = "assets/images/waves/scene6.gif";
    public static final String WAVES_EXTRA = "assets/images/waves/extra-ig.gif";
    public static final String LITTLE_BELL_SCENE_1 = "assets/images/little-bell/scene1.jpg";
    public static final String LITTLE_BELL_SCENE_2A = "assets/images/little-bell/scene2a.jpg";
    public static final String LITTLE_BELL_SCENE_2B = "assets/images/little-bell/scene2b.jpg";
    public static final String LITTLE_BELL_SCENE_3 = "assets/images/little-bell/scene3.jpg";
    public static final String LITTLE_BELL_SCENE_4 = "assets/images/little-bell/scene4.jpg";
    public static final String LITTLE_BELL_SCENE_5 = "assets/images/little-bell/scene5.jpg";
    public static final String LITTLE_BELL_SCENE_6A = "assets/images/little-bell/scene6a.jpg";
    public static final String LITTLE_BELL_SCENE_6B = "assets/images/little-bell/scene6b.jpg";
    public static final String LITTLE_BELL_SCENE_6C = "assets/images/little-bell/scene6c.jpg";
    public static final String LITTLE_BELL_SCENE_6D = "assets/images/little-bell/scene6d.jpg";

    // Music
    public static final String WAVES_DRIFT_MUSIC = "assets/rhythms/WavesSample.wav";
    public static final String WAVES_FIRST_WAVES_MUSIC = "assets/rhythms/WavesSample3.wav";
    public static final String WAVES_STRUGGLE_MUSIC = "assets/rhythms/WavesSample2.wav";
    public static final String WAVES_STEADY_RHYTHM_MUSIC = "assets/rhythms/WavesSample4.wav";
    public static final String WAVES_PATH_OF_LIGHT_MUSIC = "assets/rhythms/WavesSample5.wav";

    private AssetCatalog() {
    }

    public static URL titleScreenUrl() {
        return assetUrl(TITLE_SCREEN);
    }

    public static URL playButtonStaticUrl() {
        return assetUrl(PLAY_BUTTON_STATIC);
    }

    public static URL playButtonHoverUrl() {
        return assetUrl(PLAY_BUTTON_HOVER);
    }

    public static URL playButtonPressedUrl() {
        return assetUrl(PLAY_BUTTON_PRESSED);
    }

    public static URL optionsButtonStaticUrl() {
        return assetUrl(OPTIONS_BUTTON_STATIC);
    }

    public static URL optionsButtonHoverUrl() {
        return assetUrl(OPTIONS_BUTTON_HOVER);
    }

    public static URL optionsButtonPressedUrl() {
        return assetUrl(OPTIONS_BUTTON_PRESSED);
    }

    public static URL buttonStateUrl(String buttonId, String state) {
        return assetUrl("assets/images/buttons/" + buttonId + "_" + state + ".png");
    }

    public static URL rankUrl(String rank) {
        return assetUrl("assets/images/ranks/" + rank + "_rank.png");
    }

    public static URL wavesBackgroundUrl() {
        return assetUrl(WAVES_BACKGROUND);
    }

    public static URL backgroundUrlFor(PlayableStage stage) {
        URL[] urls = backgroundUrlsFor(stage);
        return urls.length == 0 ? null : urls[0];
    }

    public static URL gameplayBackgroundUrlFor(PlayableStage stage) {
        URL[] urls = backgroundUrlsFor(stage);
        return urls.length == 0 ? null : urls[urls.length - 1];
    }

    public static URL[] backgroundUrlsFor(PlayableStage stage) {
        if (stage instanceof WaveStage waveStage) {
            return assetUrls(backgroundsForWaveStage(waveStage));
        }

        if (stage instanceof LittleBellStage littleBellStage) {
            return assetUrls(backgroundsForLittleBellStage(littleBellStage));
        }

        return new URL[0];
    }

    public static URL cardBackgroundUrlFor(PlayableStage stage) {
        if (stage instanceof WaveStage waveStage) {
            return assetUrl(backgroundsForWaveStage(waveStage)[0]);
        }

        if (stage instanceof LittleBellStage littleBellStage) {
            return assetUrl(cardBackgroundForLittleBellStage(littleBellStage));
        }

        return null;
    }

    public static URL musicUrlFor(PlayableStage stage) {
        if (stage == WaveStage.DRIFT) {
            return assetUrl(WAVES_DRIFT_MUSIC);
        }

        if (stage == WaveStage.FIRST_WAVES) {
            return assetUrl(WAVES_FIRST_WAVES_MUSIC);
        }
        if (stage == WaveStage.STRUGGLE) {
            return assetUrl(WAVES_STRUGGLE_MUSIC);
        }
        if (stage == WaveStage.STORM) {
            return assetUrl(WAVES_STEADY_RHYTHM_MUSIC);
        }
        if (stage == WaveStage.BEYOND_THE_ISLAND || stage == WaveStage.ONE_PULL_ONE_RELEASE) {
            return assetUrl(WAVES_PATH_OF_LIGHT_MUSIC);
        }

        if (stage instanceof LittleBellStage littleBellStage) {
            return assetUrl(musicForLittleBellStage(littleBellStage));
        }

        return null;
    }

    public static boolean hasMusicFor(PlayableStage stage) {
        return musicUrlFor(stage) != null;
    }

    private static URL assetUrl(String resourcePath) {
        try {
            Path sourcePath = Path.of("src", resourcePath);
            if (Files.exists(sourcePath)) {
                return sourcePath.toUri().toURL();
            }

            return AssetCatalog.class.getResource("/" + resourcePath);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static URL[] assetUrls(String... resourcePaths) {
        URL[] urls = new URL[resourcePaths.length];

        for (int i = 0; i < resourcePaths.length; i++) {
            urls[i] = assetUrl(resourcePaths[i]);
        }

        return urls;
    }

    private static String musicForLittleBellStage(LittleBellStage stage) {
        return switch (stage) {
            case EMPTY_BASKET -> WAVES_DRIFT_MUSIC;
            case UNDER_THE_TABLE -> WAVES_FIRST_WAVES_MUSIC;
            case RAIN_ALLEY -> WAVES_STRUGGLE_MUSIC;
            case WINDOW_LIGHT -> WAVES_STEADY_RHYTHM_MUSIC;
            case THRESHOLD_LIGHT -> WAVES_PATH_OF_LIGHT_MUSIC;
            case LITTLE_BELL -> WAVES_PATH_OF_LIGHT_MUSIC;
        };
    }

    private static String[] backgroundsForWaveStage(WaveStage stage) {
        return switch (stage) {
            case DRIFT -> new String[]{WAVES_SCENE_1};
            case FIRST_WAVES -> new String[]{WAVES_SCENE_2};
            case STRUGGLE -> new String[]{WAVES_SCENE_3};
            case STORM -> new String[]{WAVES_SCENE_4};
            case BEYOND_THE_ISLAND -> new String[]{WAVES_SCENE_5};
            case ONE_PULL_ONE_RELEASE -> new String[]{WAVES_SCENE_6};
        };
    }

    private static String[] backgroundsForLittleBellStage(LittleBellStage stage) {
        return switch (stage) {
            case EMPTY_BASKET -> new String[]{LITTLE_BELL_SCENE_1};
            case UNDER_THE_TABLE -> new String[]{LITTLE_BELL_SCENE_2A, LITTLE_BELL_SCENE_2B};
            case RAIN_ALLEY -> new String[]{LITTLE_BELL_SCENE_3};
            case WINDOW_LIGHT -> new String[]{LITTLE_BELL_SCENE_4};
            case THRESHOLD_LIGHT -> new String[]{LITTLE_BELL_SCENE_5};
            case LITTLE_BELL -> new String[]{
                    LITTLE_BELL_SCENE_6A,
                    LITTLE_BELL_SCENE_6B,
                    LITTLE_BELL_SCENE_6C,
                    LITTLE_BELL_SCENE_6D
            };
        };
    }

    private static String cardBackgroundForLittleBellStage(LittleBellStage stage) {
        return switch (stage) {
            case EMPTY_BASKET -> LITTLE_BELL_SCENE_1;
            case UNDER_THE_TABLE -> LITTLE_BELL_SCENE_2A;
            case RAIN_ALLEY -> LITTLE_BELL_SCENE_3;
            case WINDOW_LIGHT -> LITTLE_BELL_SCENE_4;
            case THRESHOLD_LIGHT -> LITTLE_BELL_SCENE_5;
            case LITTLE_BELL -> LITTLE_BELL_SCENE_6A;
        };
    }
}
