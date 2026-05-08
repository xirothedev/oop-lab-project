# Candy Crush — OOP Lab Project

Match-3 game in Java 25 + JavaFX 25. Built as an OOP coursework demo for the
**Factory**, **State**, and **Observer** patterns on top of an MVC structure.

## Requirements

- Java 25
- Maven 3.8+
- OpenJFX 25 (graphics + controls modules)

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
./run.sh              # recommended launcher (sets module path)
```

Manual launch (if `./run.sh` does not match your environment):

```bash
JFX_LIB="$HOME/.m2/repository/org/openjfx"
java \
  --module-path "$JFX_LIB/javafx-base/25.0.2:$JFX_LIB/javafx-graphics/25.0.2:$JFX_LIB/javafx-controls/25.0.2" \
  --add-modules javafx.controls \
  --enable-native-access=javafx.graphics \
  --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
  -cp target/classes \
  com.ooplab.candycrush.Main
```

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
│   └── CandyFactory.java            # Factory; static create / createRandom
└── view/
    └── GameView.java                # JavaFX rendering + cell map for animations

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
