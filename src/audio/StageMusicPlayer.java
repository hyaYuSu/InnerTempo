package audio;

import config.AssetCatalog;
import model.Stages.PlayableStage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class StageMusicPlayer {
    private final Clip clip;
    private long pausedMicroseconds;
    private volatile boolean playbackCompleted;

    public StageMusicPlayer(PlayableStage stage, double volume) {
        URL musicUrl = AssetCatalog.musicUrlFor(stage);
        this.clip = musicUrl == null ? null : createClip(musicUrl);
        applyVolume(volume);
    }

    public void playFromStart() {
        if (clip == null) {
            return;
        }

        clip.stop();
        clip.setMicrosecondPosition(0);
        pausedMicroseconds = 0;
        playbackCompleted = false;
        clip.start();
    }

    public boolean hasMusic() {
        return clip != null;
    }

    public void setVolume(double volume) {
        applyVolume(volume);
    }

    public double currentTimeSeconds() {
        if (clip == null) {
            return 0;
        }

        return clip.getMicrosecondPosition() / 1_000_000.0;
    }

    public double durationSeconds() {
        if (clip == null) {
            return 0;
        }

        return clip.getMicrosecondLength() / 1_000_000.0;
    }

    public boolean isFinished() {
        if (clip == null) {
            return false;
        }

        long duration = clip.getMicrosecondLength();
        long position = clip.getMicrosecondPosition();
        return playbackCompleted || (duration > 0 && !clip.isRunning() && position >= duration - 20_000);
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
            playbackCompleted = false;
        }
    }

    public void dispose() {
        if (clip != null) {
            clip.close();
        }
    }

    private void applyVolume(double volume) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float gain;
        if (volume <= 0) {
            gain = gainControl.getMinimum();
        } else {
            gain = (float) (20.0 * Math.log10(Math.min(1.0, volume)));
            gain = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gain));
        }

        gainControl.setValue(gain);
    }

    private Clip createClip(URL musicUrl) {
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(musicUrl)) {
            Clip loadedClip = AudioSystem.getClip();
            loadedClip.open(stream);
            loadedClip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP
                        && loadedClip.getMicrosecondLength() > 0
                        && loadedClip.getMicrosecondPosition() >= loadedClip.getMicrosecondLength() - 20_000) {
                    playbackCompleted = true;
                }
            });
            return loadedClip;
        } catch (Exception ignored) {
            return null;
        }
    }
}
