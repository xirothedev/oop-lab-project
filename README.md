# Candy Crush — OOP Lab Project

Match-3 game in Java 25 + JavaFX 25. Built as an OOP coursework demo for the
**Factory**, **State**, and **Observer** patterns on top of an MVC structure.

## Requirements

- Java 25
- Maven 3.8+
- OpenJFX 25 (graphics + controls + media modules)

### Install

```bash
# macOS
brew install maven openjfx

# Ubuntu / Debian
sudo apt install maven openjfx
```

## Build & Run

```bash
mvn compile           # compile only
mvn test              # run the BoardTest suite
./run.sh              # recommended launcher; auto-detects platform classifier
```

Manual launch (if `./run.sh` does not match your environment):

```bash
# Example: macOS arm64 (mac-aarch64). For other platforms, swap the classifier
# in each jar filename: mac, linux, linux-aarch64, win.
JFX_LIB="$HOME/.m2/repository/org/openjfx"
java \
  --module-path "$JFX_LIB/javafx-base/25.0.2/javafx-base-25.0.2-mac-aarch64.jar:$JFX_LIB/javafx-graphics/25.0.2/javafx-graphics-25.0.2-mac-aarch64.jar:$JFX_LIB/javafx-controls/25.0.2/javafx-controls-25.0.2-mac-aarch64.jar:$JFX_LIB/javafx-media/25.0.2/javafx-media-25.0.2-mac-aarch64.jar" \
  --add-modules javafx.controls,javafx.media \
  --enable-native-access=javafx.graphics,javafx.media \
  --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
  -cp target/classes:target/resources \
  com.ooplab.candycrush.Main
```

Pointing `--module-path` at platform-specific jar files (instead of the parent
directories) avoids Java's "Two versions of module" error when both the
classifier jar and the non-classifier stub jar are on the path.

## Project structure

```
src/main/java/com/ooplab/candycrush/
├── Main.java                        # Entry point: wires Model + View + Controller
├── controller/
│   └── GameController.java          # Input + cascade orchestration
├── model/
│   ├── Board.java                   # 8×8 grid; swap / gravity / refill / cascade
│   ├── Cell.java                    # Position + candy slot
│   ├── Candy.java                   # Abstract base
│   ├── NormalCandy.java
│   ├── StripedCandy.java            # 4-match special; clears row or column
│   ├── CandyType.java               # 6 colors (enum)
│   ├── BlastDirection.java          # ROW | COLUMN (enum)
│   ├── MatchOrientation.java        # HORIZONTAL | VERTICAL (enum)
│   ├── MatchFinder.java             # Scans grid for runs
│   ├── MatchRun.java                # One straight run of same-type candies
│   ├── MatchResolution.java         # Cleared cells + special spawns (immutable)
│   ├── SpecialSpawn.java            # Where to place a StripedCandy
│   ├── ScoreManager.java            # Score + moves (Observer subject)
│   ├── GameState.java               # State interface
│   ├── PlayingState.java            # canPlay = true; onEnter clears status
│   └── GameOverState.java           # canPlay = false; onEnter shows final score
├── util/
│   ├── AnimationManager.java        # Animation strategy interface
│   ├── JavaFXAnimationManager.java  # JavaFX implementation
│   ├── CandyFactory.java            # Factory; static create / createRandom
│   ├── MusicManager.java            # Static BGM loop control
│   └── SoundManager.java            # Static SFX facade with cache + mute toggle
└── view/
    └── GameView.java                # JavaFX rendering + cell map for animations

src/main/resources/
├── images/                          # Candy + background sprites
│   ├── bg.png                       # Scene background
│   ├── blue.png  green.png  orange.png
│   └── purple.png  red.png  yellow.png
├── music/
│   └── bgm.mp3                      # Looping background music
└── sfx/
    ├── click.wav                    # Restart button
    ├── match.wav                    # Cascade step removal
    └── swap.wav                     # Adjacent swap

src/test/java/com/ooplab/candycrush/model/
└── BoardTest.java                   # Match resolution + striped candy behavior
```

## Design patterns

| Pattern   | Where                                                       | Notes                                                                                |
| --------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| Factory   | `util.CandyFactory`                                         | Default `Supplier<Candy>` for `Board`. Tests inject a deterministic supplier.        |
| State     | `model.GameState` + `PlayingState` + `GameOverState`        | Each state injects its own collaborators and owns the `onEnter()` side-effect.       |
| Observer  | `model.ScoreManager` ⇒ JavaFX `IntegerProperty`             | `GameController` registers listeners that push score / moves into the view.         |
| Strategy  | `util.AnimationManager` interface                           | `GameController` depends on the abstraction; `JavaFXAnimationManager` is the impl.  |

### Audio + assets

- `util.MusicManager` — static helper; `Main` calls `playBackgroundMusic()` once at startup to start the looping BGM.
- `util.SoundManager` — static facade with an internal `AudioClip` cache and a mute toggle. `GameController` triggers `playSwap()` / `playMatch()`; `GameView` triggers `playClick()` from the restart button.
- `CandyType` carries an `imagePath` alongside its color/symbol; `GameView` renders each candy as an `ImageView`, falling back to a colored rectangle if the image fails to load. The `bg.png` image is set as the StackPane background, and matches show a floating combo label via `GameView.showComboAt(...)`.

## Game rules

- 8×8 board; 6 candy types.
- Click two adjacent cells to swap.
- A move is valid only if it produces at least one run of 3 same-type candies.
- Match-3 → cells cleared, points awarded, candies fall under gravity, refilled
  from the top.
- Match-4 → spawns a `StripedCandy` at the swap target (or run endpoint for
  cascade-spawned matches). Triggering it clears the entire row or column,
  chaining further striped candies caught in the blast.
- 20 moves total. When moves hit 0 the controller transitions into `GameOverState`,
  which displays the final score.
- The Restart button resets board + score + state.

## Architecture

See [`docs/architecture.md`](docs/architecture.md) for the detailed layering,
patterns, and cascade pipeline. UML sources live in:

- `docs/uml-class.puml` — class diagram
- `docs/uml-sequence-cascade.puml` — cascade sequence diagram

Render with PlantUML:

```bash
plantuml docs/uml-class.puml docs/uml-sequence-cascade.puml
```

## Testing

Unit tests live in `src/test/java/com/ooplab/candycrush/model/BoardTest.java`
and rely on `Board(Supplier<Candy>)` plus direct `Cell.setCandy(...)` to build
deterministic boards (no JavaFX toolkit required).

```bash
mvn test
```
