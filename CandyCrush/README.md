# Candy Crush OOP Game

Match-3 game built with Java and JavaFX for OOP coursework.

## Requirements

- Java 25
- Maven 3.8+
- OpenJFX 25

### Install (macOS)

```bash
brew install maven openjfx
```

### Install (Linux/Ubuntu)

```bash
sudo apt install maven openjfx
```

## Build and Run

```bash
# Compile
mvn compile

# Run (recommended - direct Java with module path)
./run.sh

# Or manually:
JFX_LIB="$HOME/.m2/repository/org/openjfx"
java \
  --module-path "$JFX_LIB/javafx-base/25.0.2:$JFX_LIB/javafx-graphics/25.0.2:$JFX_LIB/javafx-controls/25.0.2" \
  --add-modules javafx.controls \
  --enable-native-access=javafx.graphics \
  --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
  -cp target/classes \
  com.ooplab.candycrush.Main

# Alternative Maven command (may not work on all systems):
mvn javafx:run
```

## Project Structure

```
src/main/java/com/ooplab/candycrush/
├── Main.java                    # Entry point (MVC wiring)
├── model/
│   ├── CandyType.java           # Enum: 6 candy types
│   ├── Candy.java               # Abstract base class
│   ├── NormalCandy.java         # Concrete candy
│   ├── Cell.java                # Board cell
│   ├── Board.java               # 8x8 game board
│   ├── MatchFinder.java         # Match detection
│   ├── ScoreManager.java        # Score + moves (Observer Pattern)
│   ├── GameState.java           # State Pattern interface
│   ├── PlayingState.java        # Playing state
│   └── GameOverState.java       # Game Over state
├── view/
│   └── GameView.java            # JavaFX UI
├── controller/
│   └── GameController.java      # Input + game flow
└── util/
    └── CandyFactory.java        # Factory Pattern
```

## Design Patterns Used

1. **Factory Pattern** — `CandyFactory` creates candy instances
2. **State Pattern** — `GameState` / `PlayingState` / `GameOverState`
3. **Observer Pattern** — JavaFX `Property` binding for score/moves

## Game Rules

- 8x8 board with 6 candy types
- Click two adjacent cells to swap
- Valid move: creates 3+ match in a row or column
- Matched candies are removed, points awarded
- Gravity drops remaining candies, new ones fill from top
- Cascade: combo matches after gravity are also scored
- 20 moves limit — Game Over when exhausted
- Restart button resets everything
