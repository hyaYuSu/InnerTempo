package audio;

import config.AssetCatalog;
import model.Stages.PlayableStage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class StageMusicPlayer {
    private final Clip clip;
    private long pausedMicroseconds;

    public StageMusicPlayer(PlayableStage stage) {
        URL musicUrl = AssetCatalog.musicUrlFor(stage);
        this.clip = musicUrl == null ? null : createClip(musicUrl);
    }

    public void playFromStart() {
        if (clip == null) {
            return;
        }

        clip.stop();
        clip.setMicrosecondPosition(0);
        pausedMicroseconds = 0;
        clip.start();
    }

    public boolean hasMusic() {
        return clip != null;
    }

    public double currentTimeSeconds() {
        if (clip == null) {
            return 0;
        }

        return clip.getMicrosecondPosition() / 1_000_000.0;
    }

    public void pause() {
        if (clip != null) {
            pausedMicroseconds = clip.getMicrosecondPosition();
            clip.stop();
        }
    }

    public void resume() {
        if (clip != null) {
            clip.setMicrosecondPosition(pausedMicroseconds);
            clip.start();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
        }
    }

    public void dispose() {
        if (clip != null) {
            clip.close();
        }
    }

    private Clip createClip(URL musicUrl) {
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(musicUrl)) {
            Clip loadedClip = AudioSystem.getClip();
            loadedClip.open(stream);
            return loadedClip;
        } catch (Exception ignored) {
            return null;
        }
    }
}
