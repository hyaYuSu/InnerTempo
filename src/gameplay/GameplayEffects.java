package gameplay;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GameplayEffects {
    private final List<HitBurst> hitBursts = new ArrayList<>();
    private final List<BeatPulse> beatPulses = new ArrayList<>();

    public List<HitBurst> hitBursts() {
        return hitBursts;
    }

    public List<BeatPulse> beatPulses() {
        return beatPulses;
    }

    public void addHitBurst(double x, Color color, boolean perfect, boolean gold) {
        hitBursts.add(new HitBurst(System.nanoTime(), x, color, perfect, gold));
    }

    public void addBeatPulse(boolean measureStart) {
        beatPulses.add(new BeatPulse(System.nanoTime(), measureStart));
    }

    public void prune() {
        long now = System.nanoTime();
        hitBursts.removeIf(effect -> now - effect.startedAt() > 500_000_000L);
        beatPulses.removeIf(effect -> now - effect.startedAt() > 380_000_000L);
    }

    public record HitBurst(long startedAt, double x, Color color, boolean perfect, boolean gold) {
    }

    public record BeatPulse(long startedAt, boolean measureStart) {
    }
}
