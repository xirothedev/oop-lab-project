# Architecture

## Layers

| Layer       | Package                                | Responsibility                                                              |
| ----------- | -------------------------------------- | --------------------------------------------------------------------------- |
| Model       | `com.ooplab.candycrush.model`          | Board state, candies, match detection, scoring, game-state machine.         |
| View        | `com.ooplab.candycrush.view`           | JavaFX rendering, cell panes, labels, restart button.                       |
| Controller  | `com.ooplab.candycrush.controller`     | Player input, orchestration of swap → match → gravity → cascade → score.    |
| Util        | `com.ooplab.candycrush.util`           | Cross-cutting: animation strategy, candy factory, audio (music + SFX).      |

`Main` wires the four layers together (manual DI).

## Design patterns

| Pattern   | Implemented by                                              | Notes                                                                          |
| --------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------ |
| Factory   | `util.CandyFactory`                                         | `createRandom()` is the default `Supplier<Candy>` injected into `Board`.       |
| State     | `model.GameState`, `PlayingState`, `GameOverState`          | Each state owns its own `onEnter()` UI side-effect.                            |
| Observer  | `model.ScoreManager` ⇒ JavaFX `IntegerProperty`             | `GameController.setupBindings()` registers listeners that push label updates. |
| Strategy  | `util.AnimationManager` (interface) + `JavaFXAnimationManager` | Controller depends on the abstraction; tests can inject a synchronous stub.   |
| Static facade | `util.MusicManager`, `util.SoundManager`                | Class-level static methods, not Singletons. `MusicManager` owns the BGM `MediaPlayer`; `SoundManager` caches `AudioClip` instances by key and exposes a mute toggle. Used directly by `Main` (BGM start), `GameController` (swap / match SFX), and `GameView` (click SFX). |

## Key types

| Type                | Role                                                                                                |
| ------------------- | --------------------------------------------------------------------------------------------------- |
| `Board`             | 8×8 grid; owns swap, `resolveMatches`, gravity, refill, reset.                                      |
| `MatchFinder`       | Pure function over a grid → list of `MatchRun`.                                                     |
| `MatchRun`          | Maximal straight run of same-type candies (≥3).                                                     |
| `MatchResolution`   | Immutable result of one resolve step: cells to clear + special candies to spawn.                    |
| `SpecialSpawn`      | Where to place a `StripedCandy` after a 4-match.                                                    |
| `Candy` hierarchy   | `NormalCandy` and `StripedCandy(BlastDirection)`.                                                   |
| `CandyType`         | Enum carrying color, symbol, and `imagePath` (for `ImageView` rendering with rectangle fallback).    |
| `ScoreManager`      | Observable score + moves; `isGameOver()` when moves hit 0.                                          |
| `GameController`    | Single owner of the cascade; flattened pipeline (`cascadeStep` → `applyResolutionAndContinue` → …). |
| `GameView`          | Renders the grid into a `Map<Cell, StackPane>` so the controller can target animations by cell. Uses `bg.png` as scene background, candy `ImageView` children with rectangle fallback, styled restart button, and `showComboAt(...)` floating-label animation. |
| `MusicManager`      | Static helper; `Main.start` calls `playBackgroundMusic()` once to begin the looping BGM.            |
| `SoundManager`      | Static facade over a cached `Map<String, AudioClip>` plus mute toggle. Exposes `playSwap()`, `playMatch()`, `playClick()`. |

## Cascade pipeline

`attemptSwap → playSwap → resolveSwap → cascadeStep → playRemoval → applyResolutionAndContinue → playGravity → playSpawn → cascadeStep …`

When `MatchResolution.isEmpty()`, `finishCascade()` decrements moves and—if the game is over—transitions to `gameOverState`.

## Test seams

- `Board(Supplier<Candy>)` lets tests build a deterministic board without mocking statics.
- `AnimationManager` interface lets tests pass a stub that fires `callback.run()` synchronously, breaking the JavaFX threading dependency.
- States can be unit-tested by injecting a `GameView` test double.

## Diagrams

- `docs/uml-class.puml` — class diagram with relationships and stereotypes.
- `docs/uml-sequence-cascade.puml` — sequence diagram of a full cascade after a player swap.

Render with PlantUML:

```bash
plantuml docs/uml-class.puml docs/uml-sequence-cascade.puml
```
