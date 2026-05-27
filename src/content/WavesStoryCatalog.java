package content;

import model.Stages.WaveStage;

public final class WavesStoryCatalog {
    private static final String PRE_GAME_PROLOGUE = """
            Me:
            The world stopped making sense a long time ago. Now, there is only the water. I don't know if I'm moving forward or just drifting in circles.

            The Wind:
            Direction is a luxury of the shore. Out here, the only thing that matters is the rhythm. If you stop moving, the silence will swallow you. If you move too fast, the waves will break you.
            """;

    private static final String SCENE_1 = """
            Narrator:
            The night is wide and quiet. A dark blue ocean stretches in every direction. No shore. No stars bright enough to guide the way. Only a small wooden rowboat, floating gently on the dark water.

            Me:
            I don't know where I am.

            Narrator:
            The character sits in the center of the boat, hands wrapped tightly around two heavy oars. The ocean is calm, but the silence feels too deep.

            Me:
            I should feel something. Fear. Hope. Anything. But I just feel... empty.

            The Wind:
            You do not have to understand everything right now. Just notice where you are.

            Me:
            Who are you?

            The Wind:
            I'm just someone you can rely on, someone who has seen the horizon you haven't reached yet. I am the breath you just took and the space between the thoughts you're trying to catch.

            Me:
            But you're just air. You don't have hands. You can't help me row.

            The Wind:
            True, I cannot pull the oars for you. But I can tell you that the water beneath you isn't an enemy. It's just a path that hasn't been written on yet. Why do you hold the wood so tightly?

            Me:
            Because if I let go, I'm just a piece of drift. If I'm not rowing, am I even moving?

            The Wind:
            You are always moving, even when the oars are still. The current has its own rhythm. You're so afraid of being lost that you've forgotten how to just be present.

            Me:
            Being present feels like being nothing.

            The Wind:
            Then be nothing for a moment. It's much lighter to carry than everything.

            Narrator:
            The boat rocks softly. The ocean does not answer, but it does not pull them under either.

            The Wind:
            That is enough for the first moment.
            """;

    private static final String SCENE_2 = """
            Narrator:
            The character begins to paddle. At first, the oars move slowly through the water. Pull. Release. Pull. Release.

            Me:
            Maybe if I keep moving, I'll find a way out.

            Narrator:
            Then the water shifts. Small waves slap against the side of the boat. Not strong enough to sink it, but sharp enough to disturb the silence.

            Me:
            Why is this happening now? I was doing fine.

            The Wind:
            Some waves arrive without warning. That does not mean you have failed.

            Me:
            They're small, but they're getting under my skin.

            The Wind:
            Then start small too. One breath. One pull. One release.
            """;

    private static final String SCENE_3 = """
            Narrator:
            Without warning, the ocean turns rough. The small waves become tall and uneven. The boat rises, drops, and shakes beneath the character.

            Me:
            I can't do this. It's too much.

            Narrator:
            A wave crashes against the boat. Cold water splashes over their hands.

            Me:
            I want to stop. I want to pull the oars in and let go.

            The Wind:
            You do not have to defeat the storm.

            Me:
            Then what am I supposed to do?

            The Wind:
            Stay with the next movement. Not the whole ocean. Just the next movement.

            Me:
            My hands are shaking.

            Inner Voice:
            Shaking hands can still hold on.

            Narrator:
            The boat leans hard to one side, but it does not capsize.
            """;

    private static final String SCENE_4 = """
            Narrator:
            The character stops fighting every wave. Instead of paddling harder, they paddle steadier.

            Me:
            Pull... breathe in. Release... breathe out.

            Narrator:
            The storm still moves around them, but the character begins to notice the space between each crash.

            Me:
            There's a pause. Even here, there's a pause.

            The Wind:
            Yes. Rest can exist even inside difficulty.

            Me:
            I thought I had to be calm for the storm to stop.

            The Wind:
            No. You can be unsteady and still continue. You can be afraid and still breathe.
            """;

    private static final String SCENE_5 = """
            Narrator:
            The storm does not disappear. The ocean remains vast, dark, and uncertain. But something changes.

            Me:
            What is that?

            Narrator:
            A faint path of light appears on the water. It does not lead to a shore. It does not reveal the end of the ocean. It simply lights the space around the boat.

            The Wind:
            You are still here.

            Me:
            I'm still in the middle of it.

            The Wind:
            Yes. But you are no longer lost in the same way.

            Me:
            The storm is still here... but my hands are steadier.

            Narrator:
            The character looks down at the oars, then at the glowing water around the boat.

            Me:
            I don't have to control the whole ocean. I just have to keep my rhythm.

            The Wind:
            One breath. One pull. One release.
            """;

    private static final String SCENE_6 = """
            Narrator:
            The darkness hasn't vanished, but it no longer feels like a wall. The horizon remains a mystery, yet the character no longer stares at it with longing or dread. They look only at the circle of light where the wood of the oars meets the surface of the sea.

            Me:
            The shore is still a long way off, isn't it?

            The Wind:
            Perhaps. But look at the water you've already crossed. You didn't think you could carry yourself this far, yet here you are.

            Me:
            (A small, tired smile)
            I'm tired. But I'm not empty anymore. I'm filled with the effort of being alive.

            The Wind:
            (A soft, lingering whistle)
            The effort is where the light comes from. Keep going. You are the navigator and the vessel all at once.

            Narrator:
            The character leans into the next stroke. The sound of the oars dipping into the water is rhythmic, like a second heartbeat. The boat moves, not with frantic speed, but with a quiet, undeniable purpose.

            Me:
            One pull. One release.

            Narrator:
            The light follows the boat, a steady companion in the vastness.
            """;

    private static final String END_OF_GAME = """
            Narrator:
            The ocean is still vast, and the night is still long. But you have found the only thing that cannot be taken: your own breath. You are not drifting. You are traveling.

            Me:
            I am still here.
            """;

    private WavesStoryCatalog() {
    }

    public static String summaryFor(WaveStage stage) {
        return switch (stage) {
            case DRIFT -> "A quiet rowboat drifts on dark water while the first breath returns.";
            case FIRST_WAVES -> "Small waves disturb the silence, and the rhythm begins with one pull and one release.";
            case STRUGGLE -> "The ocean turns rough, and the next movement becomes enough.";
            case STORM -> "The storm continues, but a steadier rhythm appears inside it.";
            case BEYOND_THE_ISLAND -> "A faint path of light gathers around the boat.";
            case ONE_PULL_ONE_RELEASE -> "The horizon remains unknown, but the boat moves with quiet purpose.";
        };
    }

    public static String storyFor(WaveStage stage) {
        return switch (stage) {
            case DRIFT -> PRE_GAME_PROLOGUE + "\n\n" + SCENE_1;
            case FIRST_WAVES -> SCENE_2;
            case STRUGGLE -> SCENE_3;
            case STORM -> SCENE_4;
            case BEYOND_THE_ISLAND -> SCENE_5;
            case ONE_PULL_ONE_RELEASE -> SCENE_6;
        };
    }

    public static String resultFor(WaveStage stage, boolean cleared) {
        if (stage == WaveStage.ONE_PULL_ONE_RELEASE && cleared) {
            return END_OF_GAME;
        }

        if (!cleared) {
            return switch (stage) {
                case DRIFT -> "The silence feels heavy again, and the boat drifts without answer.\n\nBut the oars are still in your hands.";
                case FIRST_WAVES -> "The small waves scatter the rhythm, and the pull of the oars turns uneven.\n\nThe Wind stays close. One breath is still enough to begin again.";
                case STRUGGLE -> "The rough water wins this moment. Cold spray covers your hands, and stopping feels easier than staying.\n\nStill, shaking hands can hold on.";
                case STORM -> "The storm drowns out the pause between each crash.\n\nSomewhere under the noise, the next breath is still waiting.";
                case BEYOND_THE_ISLAND -> "The faint light flickers on the water, close enough to see but hard to follow.\n\nYou are still here, even if the rhythm slips.";
                case ONE_PULL_ONE_RELEASE -> "The horizon remains hidden, and the next stroke feels far away.\n\nBut the boat has not stopped moving.";
            };
        }

        return switch (stage) {
            case DRIFT -> "The boat rocks softly beneath you. For the first time, the stillness does not feel like failure.\n\nYou loosen your grip on the oars. That is enough for the first moment.";
            case FIRST_WAVES -> "The oars move through the water again. Pull. Release. Pull. Release.\n\nThe waves are still there, but your breath has found something to follow.";
            case STRUGGLE -> "The boat leans hard to one side, but it does not capsize.\n\nYou do not defeat the storm. You stay with the next movement.";
            case STORM -> "The storm stays loud, but you begin to hear the quiet space between each crash.\n\nYou are unsteady, afraid, and still breathing.";
            case BEYOND_THE_ISLAND -> "A faint light gathers around the boat. It does not show the shore, but it shows that you are still here.\n\nYour hands are steadier on the oars.";
            case ONE_PULL_ONE_RELEASE -> END_OF_GAME;
        };
    }
}
