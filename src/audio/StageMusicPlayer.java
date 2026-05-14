package audio;

import model.Stages.PlayableStage;
import model.Stages.WaveStage;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class StageMusicPlayer {
    private final MediaPlayer mediaPlayer;

    public StageMusicPlayer(PlayableStage stage) {
        String musicUri = musicUriFor(stage);
        this.mediaPlayer = musicUri == null ? null : createPlayer(musicUri);
    }

    public void playFromStart() {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.seek(Duration.ZERO);
        mediaPlayer.play();
    }

    public boolean hasMusic() {
        return mediaPlayer != null;
    }

    public double currentTimeSeconds() {
        if (mediaPlayer == null) {
            return 0;
        }

        return mediaPlayer.getCurrentTime().toSeconds();
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }

    private MediaPlayer createPlayer(String musicUri) {
        MediaPlayer player = new MediaPlayer(new Media(musicUri));
        player.setVolume(0.75);
        return player;
    }

    private String musicUriFor(PlayableStage stage) {
        if (stage == WaveStage.DRIFT) {
            return assetUri("assets/rhythms/WavesSample.wav", Path.of("src", "assets", "rhythms", "WavesSample.wav"));
        }

        if (stage == WaveStage.FIRST_WAVES) {
            return assetUri("assets/rhythms/WavesSample2.wav", Path.of("src", "assets", "rhythms", "WavesSample2.wav"));
        }

        return null;
    }

    private String assetUri(String resourcePath, Path sourcePath) {
        if (Files.exists(sourcePath)) {
            return sourcePath.toUri().toString();
        }

        URL resource = StageMusicPlayer.class.getResource("/" + resourcePath);
        return resource == null ? null : resource.toExternalForm();
    }
}
