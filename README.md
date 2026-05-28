# Inner Tempo

## Description

Inner Tempo is a Java desktop rhythm game built with Swing. The player progresses through story-based journeys, plays rhythm stages, earns scores, unlocks new scenes, and follows narrative chapters supported by local image, GIF, and music assets.

The game currently includes two journeys:

- WAVES - Finding Direction in the Middle of the Storm
- LITTLE BELL - Coming Home Without What Was Lost

## Features

- Story-based rhythm gameplay
- Multiple journeys with unlockable stages
- Arrow-key rhythm controls
- Perfect, Great, Good, Bad, and Miss judgment system
- Score, combo, accuracy, and rank tracking
- Stage progression based on clear accuracy
- Local save system for unlocked stages, cleared stages, high scores, and best accuracy
- Title screen, main menu, journey selection, gameplay, pause menu, options screen, story screens, and results screen
- Custom visual and audio assets

## Tech Stack

- Language: Java
- GUI Framework: Java Swing
- Audio: Java Sound API
- IDE: IntelliJ IDEA
- Project Type: Java desktop application
- Assets: GIF, JPG, PNG, and WAV files

## Project Structure

```text
InnerTempo/
+-- src/
|   +-- main/                    # Main entry point of the game
|   +-- screens/                 # Title, menu, gameplay, options, story, and results screens
|   +-- gameplay/                # Rhythm logic, chart generation, input handling, and hit judging
|   +-- gameplay/stageCharts/    # Manual rhythm chart definitions
|   +-- model/                   # Notes, journeys, journey scenes, and playable stages
|   +-- manager/                 # Screen navigation and save management
|   +-- score/                   # Score, combo, accuracy, and judgment tracking
|   +-- settings/                # Gameplay settings, controls, timing, and volume
|   +-- config/                  # Game constants and asset catalog
|   +-- content/                 # Journey and story text content
|   +-- audio/                   # Stage music player
|   +-- ui/                      # Reusable UI components and renderers
|   +-- assets/
|       +-- images/              # Backgrounds, buttons, ranks, and story images
|       +-- rhythms/             # WAV music and rhythm files
+-- README.md                    # Project documentation
```

## Run

The main entry point of the project is the `main.Main` class.

The project module is currently configured with OpenJDK 26.

## OOP Pillars Used

### Inheritance

The project uses inheritance in its custom UI components. Several classes extend Java Swing classes so they can reuse existing window and panel behavior while adding custom game visuals.

Examples:

- `GradientPanel extends JPanel`
- `WarmCinematicImagePanel extends JPanel`
- `RhythmGameScreen.GamePanel extends JPanel`

These classes inherit the behavior of `JPanel` and override methods such as `paintComponent()` to draw custom backgrounds, gameplay lanes, notes, and visual effects.

### Abstraction

The project uses abstraction through the `PlayableStage` interface. This interface defines what every playable stage must provide, such as its journey ID, stage number, title, next stage, and save key.

Example:

```java
public interface PlayableStage {
    JourneyId getJourneyId();
    int getNumber();
    int getIndex();
    String getTitle();
    PlayableStage next();
    String getSaveKeyPrefix();
}
```

This allows the rest of the program to work with a general `PlayableStage` type without needing to know the exact stage category.

### Polymorphism

The project uses polymorphism because different stage types can be treated as the same general type, `PlayableStage`.

Examples:

- `WaveStage implements PlayableStage`
- `LittleBellStage implements PlayableStage`

Classes such as `ScreenManager`, `SaveManager`, `RhythmGameScreen`, and `AssetCatalog` accept `PlayableStage` objects. This means the same gameplay, saving, scoring, and asset-loading logic can work with both WAVES stages and LITTLE BELL stages.

### Encapsulation

The project uses encapsulation by keeping class data private and accessing it through methods. This protects important game data from being changed directly in unsafe ways.

Examples:

- `Note` stores note timing, lane, hold status, and hit status using private fields.
- `Journey` stores journey details such as title, subtitle, description, and scenes using private fields.
- `ScoreTracker` stores score, combo, accuracy, and judgment counts using private fields.
- `GameplaySettings` stores volume, note speed, timing settings, and key bindings using private fields.

For example, other classes do not directly edit the score fields in `ScoreTracker`. They use methods such as `record()`, `recordHoldTick()`, `getScore()`, `getAccuracy()`, and `getGrade()`.

## Contributors

- Hyacinth Sumadia
- Ashe Vencio
- Charles Dizon
