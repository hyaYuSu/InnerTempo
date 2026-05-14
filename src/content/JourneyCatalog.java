package content;

import model.Journey;
import model.JourneyId;
import model.JourneyScene;
import model.Stages.LittleBellStage;
import model.Stages.WaveStage;

import java.util.List;

public final class JourneyCatalog {
    private JourneyCatalog() {
    }

    public static Journey byId(JourneyId id) {
        return switch (id) {
            case WAVES -> waves();
            case GATO -> stageTwo();
        };
    }

    public static List<Journey> unlockedJourneys() {
        return List.of(waves(), stageTwo());
    }

    public static Journey waves() {
        return new Journey(
                JourneyId.WAVES,
                "Waves",
                "Ride what comes.",
                "A quiet ocean journey about learning to meet life the way waves arrive: one uncertain swell at a time.",
                "Ocean Background GIF",
                List.of(
                        waveScene(WaveStage.DRIFT, "A board, a quiet shore, and the first wave that asks for courage."),
                        waveScene(WaveStage.FIRST_WAVES, "Small waves teach the body how to try again."),
                        waveScene(WaveStage.STRUGGLE, "Falling becomes part of learning how to continue."),
                        waveScene(WaveStage.STORM, "The water grows loud, and fear has to ride along."),
                        waveScene(WaveStage.BEYOND_THE_ISLAND, "The wave is no longer an enemy. It is a way forward.")
                )
        );
    }

    public static Journey stageTwo() {
        return new Journey(
                JourneyId.GATO,
                "Little Bell",
                "A small search for something that used to answer.",
                "A cat wanders through familiar places looking for a lost toy. Underneath the search is the feeling of missing an old friend.",
                "Warm Room Background",
                List.of(
                        catScene(
                                LittleBellStage.EMPTY_BASKET,
                                "A warm room, a sunlit floor, and a missing cloth mouse.",
                                "The basket still smelled like morning.\n\n"
                                        + "Sunlight warmed the floorboards.\n\n"
                                        + "The cat stepped inside, turned once, then stopped.\n\n"
                                        + "Something was missing.\n\n"
                                        + "Not food. Not water. Not the hand that sometimes scratched behind its ears.\n\n"
                                        + "The small cloth mouse with the little bell was gone.\n\n"
                                        + "The cat lowered its head and listened.\n\n"
                                        + "No bell.\n\n"
                                        + "Only the slow tick of the room.",
                                true,
                                ""
                        ),
                        catScene(
                                LittleBellStage.UNDER_THE_TABLE,
                                "Chair legs, dust, crumbs, and a sound that almost feels familiar.",
                                "Under the table, the world became legs and shadows.\n\n"
                                        + "The cat sniffed between chair feet and dust.\n\n"
                                        + "A button rolled when its paw touched it.\n\n"
                                        + "For one bright second, it sounded like the bell.\n\n"
                                        + "The cat froze.\n\n"
                                        + "Then the sound stopped.\n\n"
                                        + "It was only a button.",
                                false,
                                "Clear Empty Basket to unlock."
                        ),
                        catScene(
                                LittleBellStage.RAIN_ALLEY,
                                "Rain taps the pavement while a faraway chime pulls the cat outside.",
                                "The door had been left open.\n\n"
                                        + "The cat slipped into the alley.\n\n"
                                        + "Rain tapped the ground in tiny silver beats.\n\n"
                                        + "Somewhere far away, metal chimed.\n\n"
                                        + "The cat ran toward it.",
                                false,
                                "Clear Under the Table to unlock."
                        ),
                        catScene(
                                LittleBellStage.WINDOW_LIGHT,
                                "A warm window glows above the street, close enough to remember but too far to enter.",
                                "The cat found a window glowing above the street.\n\n"
                                        + "Inside, someone laughed.\n\n"
                                        + "For a moment, the sound felt familiar.\n\n"
                                        + "The cat pressed one paw to the glass.\n\n"
                                        + "The room beyond was warm.\n\n"
                                        + "But the bell was not there.",
                                false,
                                "Clear Rain Alley to unlock."
                        ),
                        catScene(
                                LittleBellStage.LITTLE_BELL,
                                "At sunset, the cat finds what remains and carries its small sound home.",
                                "At the edge of the steps, something small caught the light.\n\n"
                                        + "The cloth mouse was not there.\n\n"
                                        + "But the little bell was.\n\n"
                                        + "The cat nudged it once.\n\n"
                                        + "A small sound answered.\n\n"
                                        + "Not the same as before.\n\n"
                                        + "But real.\n\n"
                                        + "The cat carried it home carefully.\n\n"
                                        + "Some things do not come back whole.\n\n"
                                        + "But some part of them still rings.",
                                false,
                                "Clear Window Light to unlock."
                        )
                )
        );
    }

    private static JourneyScene waveScene(WaveStage stage, String summary) {
        return new JourneyScene(
                stage.getNumber(),
                stage.getTitle(),
                summary,
                "",
                "Clear Scene " + (stage.getNumber() - 1) + " with 70% accuracy.",
                stage.getNumber() == 1,
                stage
        );
    }

    private static JourneyScene catScene(
            LittleBellStage stage,
            String summary,
            String storyText,
            boolean unlockedByDefault,
            String lockedReason
    ) {
        return new JourneyScene(
                stage.getNumber(),
                stage.getTitle(),
                summary,
                storyText,
                lockedReason,
                unlockedByDefault,
                stage
        );
    }
}
