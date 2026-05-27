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
                "WAVES",
                "Finding Direction in the Middle of the Storm",
                "In a sea with no visible shore, a drifting soul discovers that each breath can become an oar and each movement can become a way forward.",
                "Dark Ocean Background",
                List.of(
                        waveScene(WaveStage.DRIFT),
                        waveScene(WaveStage.FIRST_WAVES),
                        waveScene(WaveStage.STRUGGLE),
                        waveScene(WaveStage.STORM),
                        waveScene(WaveStage.BEYOND_THE_ISLAND),
                        waveScene(WaveStage.ONE_PULL_ONE_RELEASE)
                )
        );
    }

    public static Journey stageTwo() {
        return new Journey(
                JourneyId.GATO,
                "LITTLE BELL",
                "Coming Home Without What Was Lost",
                "A stray cat crosses a fog-drenched city with an empty basket and learns that being seen is enough.",
                "Warm Room Background",
                List.of(
                        catScene(
                                LittleBellStage.EMPTY_BASKET,
                                "The fog is thick, the basket is empty, and nothing feels heavy.",
                                story(
                                        "Little Bell:\n\"The fog is thick enough to swallow my paws. I have this basket, but it's full of nothing. Why does nothing feel so heavy?\"",
                                        "The City:\n\"Everything has a place. The bricks have the walls. The water has the gutter. But you... you have a hole where your heart used to be.\"",
                                        "Narrator:\nA quiet, fog-drenched city alleyway. The sounds of the city are muffled, but every drip of water or distant footstep feels amplified. Little Bell, a small stray cat, walks slowly, teeth gripped tight around the handle of an empty woven basket.",
                                        "Little Bell:\n\"I can't find it. I've looked behind every dumpster and under every parked car. If I don't find the toy, I'm just a cat with a useless basket.\"",
                                        "Inner Voice:\n\"The basket isn't empty. It's full of your failure. Everyone who sees you knows you've lost the only thing that made you special.\"",
                                        "Little Bell:\n\"My jaws ache. But if I put it down, I have nothing left at all.\""
                                ),
                                true,
                                ""
                        ),
                        catScene(
                                LittleBellStage.UNDER_THE_TABLE,
                                "Behind cold crates, every shadow looks like a reaching hand.",
                                story(
                                        "Narrator:\nLittle Bell creeps behind a row of cold wooden crates. Every shadow looks like a reaching hand; every rustle of trash sounds like a whispered judgment.",
                                        "Little Bell:\n(Jumping at a shadow)\n\"Who's there? Are you watching me? Are you laughing because my basket is empty?\"",
                                        "The City:\n\"The city doesn't laugh. It just watches. It sees a cat looking for a ghost. It sees a cat who doesn't know that the 'old version' of himself is buried under the concrete.\"",
                                        "Little Bell:\n\"I just want to be who I was. I want the toy back. I want the joy back. Why is the labyrinth so long?\""
                                ),
                                false,
                                "Clear Empty Basket to unlock."
                        ),
                        catScene(
                                LittleBellStage.RAIN_ALLEY,
                                "Under rusted stairs, the cold begins to sound like blame.",
                                story(
                                        "Narrator:\nLittle Bell stops beneath a set of rusted fire escape stairs. It's dark here, shielded from the fog, but the air is freezing.",
                                        "Little Bell:\n\"I remember playing here. The toy was bright red. It smelled like home. Now, everything just smells like wet iron and old rain.\"",
                                        "Inner Voice:\n\"The toy is gone because you didn't look after it. You don't deserve the 'old you.' You only deserve this cold, dark corner.\"",
                                        "Little Bell:\n\"Maybe if I just sit here long enough, the world will forget I exist. Then the empty basket won't matter.\""
                                ),
                                false,
                                "Clear Under the Table to unlock."
                        ),
                        catScene(
                                LittleBellStage.WINDOW_LIGHT,
                                "The basket feels like lead, and the city says the labyrinth has won.",
                                story(
                                        "Narrator:\nLittle Bell tries to stand, but the basket feels like it's made of lead. The exhaustion of being afraid and being lonely finally catches up.",
                                        "Little Bell:\n\"I can't go another block. My legs are shaking. I'm lost, and the toy is gone, and I'm just... I'm just tired.\"",
                                        "The City:\n\"The labyrinth has won. You are just another piece of the alleyway now.\"",
                                        "Little Bell:\n\"I don't want to be a piece of the alley. I want to be warm. But I have nothing to trade for warmth. I have an empty basket.\""
                                ),
                                false,
                                "Clear Rain Alley to unlock."
                        ),
                        catScene(
                                LittleBellStage.THRESHOLD_LIGHT,
                                "A warm yellow light cuts through the fog like a golden path.",
                                story(
                                        "Narrator:\nLittle Bell turns a corner and stops. In front of a small, humble house, a warm yellow light spills out from beneath the door, cutting through the fog like a golden path.",
                                        "Little Bell:\n\"It's so bright. It looks... safe. But I can't go there. I'm dirty, and my basket is empty. They'll see how broken I am.\"",
                                        "Inner Voice:\n\"Hide. Stay in the fog. The light is for cats who have their toys. The light is for the whole ones.\"",
                                        "Little Bell:\n(Trembling)\n\"I'm tired of hiding. Even if they turn me away... I just want to feel the warmth for one second.\""
                                ),
                                false,
                                "Clear Window Light to unlock."
                        ),
                        catScene(
                                LittleBellStage.LITTLE_BELL,
                                "In the center of the light, Little Bell is seen without the missing toy.",
                                story(
                                        "Narrator:\nLittle Bell sits in the center of the light, vulnerable and shivering. The door creaks open. A soft, kind voice calls out into the night.",
                                        "Kind Voice:\n\"Oh... hello there, little one. You look like you've traveled a long way.\"",
                                        "Little Bell:\n\"I don't have my toy. My basket is empty. I'm not who I used to be.\"",
                                        "Narrator:\nThe person doesn't look at the basket. They look at the cat. They reach out a hand - not to judge, but to welcome.",
                                        "Little Bell:\n\"They're... they're looking at me. Not the void. Just me. I don't need the toy to be worthy of the light. I just need to be here.\"",
                                        "Narrator:\n\"The old toy is gone, and the path back is closed. But a new path has opened. You are not a 'version' of yourself to be found; you are a living soul to be cared for. The basket is empty, but your heart is finally coming home.\"",
                                        "Little Bell:\n\"I am seen. And that is enough.\""
                                ),
                                false,
                                "Clear Threshold Light to unlock."
                        )
                )
        );
    }

    private static JourneyScene waveScene(WaveStage stage) {
        return new JourneyScene(
                stage.getNumber(),
                stage.getTitle(),
                WavesStoryCatalog.summaryFor(stage),
                WavesStoryCatalog.storyFor(stage),
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

    private static String story(String... paragraphs) {
        return String.join("\n\n", paragraphs);
    }
}
