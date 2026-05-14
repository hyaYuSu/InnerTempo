package gameplay;

import model.Note;
import model.NoteType;

import java.util.ArrayList;
import java.util.List;

public class ChartBuilder {
    private final StageBeatProfile timing;
    private final List<Note> notes = new ArrayList<>();

    public ChartBuilder(StageBeatProfile timing) {
        this.timing = timing;
    }

    public List<Note> notes() {
        return notes;
    }

    public void taps(int measure, int[] slots, int[] lanes) {
        validatePattern(slots, lanes);

        for (int i = 0; i < slots.length; i++) {
            notes.add(new Note(time(measure, slots[i]), lanes[i]));
        }
    }

    public void hold(int measure, int slot, int lane, int durationSlots) {
        notes.add(new Note(time(measure, slot), lane, durationSlots * timing.gridSlotSeconds()));
    }

    public void gold(int measure, int slot, int lane) {
        notes.add(new Note(time(measure, slot), lane, NoteType.GOLD));
    }

    public int[] lanesFor(int measure, int... baseLanes) {
        int[] lanes = new int[baseLanes.length];

        for (int i = 0; i < baseLanes.length; i++) {
            lanes[i] = laneFor(measure, baseLanes[i]);
        }

        return lanes;
    }

    public int laneFor(int measure, int baseLane) {
        return Math.floorMod(baseLane + measure, 4);
    }

    private double time(int measure, int slot) {
        return timing.timeForGridSlot(measure, slot);
    }

    private void validatePattern(int[] slots, int[] lanes) {
        if (slots.length != lanes.length) {
            throw new IllegalArgumentException("Tap slots and lanes must have the same length.");
        }
    }
}
