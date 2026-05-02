package com.ooplab.candycrush.controller;

import com.ooplab.candycrush.model.*;
import com.ooplab.candycrush.util.AnimationManager;
import com.ooplab.candycrush.util.SoundManager;
import com.ooplab.candycrush.view.GameView;
import javafx.animation.PauseTransition;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.Glow;
import javafx.util.Duration;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller: handles player input, coordinates game logic between model and view.
 * Manages the full game flow with animations: swap → match check → gravity → cascade → score → game over.
 */
public class GameController {
    private final Board board;
    private final ScoreManager scoreManager;
    private final GameView view;
    private final AnimationManager animationManager;
    private int combo = 0;

    private GameState state;
    private final GameState playingState;
    private final GameState gameOverState;

    private Cell selectedCell;
    private final AtomicBoolean isAnimating = new AtomicBoolean(false);

    public GameController(Board board, ScoreManager scoreManager, GameView view) {
        this.board = board;
        this.scoreManager = scoreManager;
        this.view = view;
        this.animationManager = new AnimationManager();

        this.playingState = new PlayingState();
        this.gameOverState = new GameOverState();
        this.state = playingState;

        setupView();
        refreshView();
        setupBindings();
    }

    private void setupView() {
        view.renderBoard(board, this::handleCellClick);
        view.setOnRestart(this::restart);
        setupBindings();
    }

    private void setupBindings() {
        scoreManager.scoreProperty().addListener((obs, oldVal, newVal) ->
                view.setScoreText("Score: " + newVal)
        );
        scoreManager.movesProperty().addListener((obs, oldVal, newVal) ->
                view.setMovesText("Moves: " + newVal)
        );
    }

    private void handleCellClick(Cell clicked) {
        if (!state.canPlay() || isAnimating.get()) {
            return;
        }

        if (selectedCell == null) {
            selectedCell = clicked;
            view.highlightCell(clicked);
            view.setStatusText("");
        } else {
            Cell first = selectedCell;
            selectedCell = null;

            if (first == clicked) {
                view.clearSelection();
                view.setStatusText("");
                return;
            }

            if (isAdjacent(first, clicked)) {
                attemptSwap(first, clicked);
            } else {
                selectedCell = clicked;
                view.highlightCell(clicked);
                view.setStatusText("");
            }
        }
    }

    private boolean isAdjacent(Cell a, Cell b) {
        int rowDiff = Math.abs(a.getRow() - b.getRow());
        int colDiff = Math.abs(a.getCol() - b.getCol());
        return (rowDiff + colDiff) == 1;
    }

    private void attemptSwap(Cell a, Cell b) {
        isAnimating.set(true);
        view.setAnimating(true);
        SoundManager.playSwap();

        StackPane paneA = view.getCellPane(a);
        StackPane paneB = view.getCellPane(b);

        // Step 1: Animate swap
        animationManager.playSwap(paneA, paneB, () -> {
            // Step 2: After swap animation completes, check for matches
            board.swap(a, b);
            view.renderBoard(board, this::handleCellClick);
            var matches = board.findMatches();

            if (matches.isEmpty()) {
                // No match — revert swap, animate back
                board.swap(a, b);
                animationManager.playSwapBack(paneA, paneB, () -> {
                    view.renderBoard(board, this::handleCellClick);
                    view.setStatusText("Invalid move!");
                    unlockInput();
                });
                return;
            }

            // Valid match — start cascade
            cascadeStep(matches);
        });
    }

    /**
     * One step of the cascade: remove matched → animate removal →
     * apply gravity → animate gravity → fill new → animate spawn →
     * check for next cascade step.
     */
    private void cascadeStep(Set<Cell> matches) {
        if (matches.isEmpty()) {
            combo = 0;
            // Cascade complete
            scoreManager.useMove();
            view.renderBoard(board, this::handleCellClick);
            unlockInput();

            if (scoreManager.isGameOver()) {
                transitionToGameOver();
            }
            return;
        }

        // Collect panes for matched cells (for removal animation)
        List<StackPane> removalPanes = new ArrayList<>();
        for (Cell cell : matches) {
            StackPane pane = view.getCellPane(cell);
            if (pane != null) {
                removalPanes.add(pane);
            }
        }

        //Animate removal
        animationManager.playRemoval(removalPanes, () -> {
            // After removal animation, apply model changes
            board.removeMatches(matches);
            //Combo
            combo++;

            int base = matches.size();
            int score = base * combo;

            scoreManager.addScore(score);

            if (combo == 1) {
                SoundManager.playMatch();
            } else if (combo == 2) {
                SoundManager.playMatch(); // sau này đổi file khác
            } else {
                SoundManager.playMatch(); // mạnh hơn nếu có
            }
            if (combo > 1) {
                Cell any = matches.iterator().next();

                PauseTransition delay = new PauseTransition(Duration.millis(50));
                delay.setOnFinished(e ->
                        view.showComboAt(any, "Combo x" + combo + "!")
                );
                delay.play();
            }

            Map<Cell, Integer> gravityDrops = board.applyGravity();

            // Fill empty cells with new candies
            List<Cell> newCells = new ArrayList<>();
            for (int r = 0; r < Board.SIZE; r++) {
                for (int c = 0; c < Board.SIZE; c++) {
                    Cell cell = board.getCell(r, c);
                    if (cell.isEmpty()) {
                        cell.setCandy(com.ooplab.candycrush.util.CandyFactory.createRandom());
                        newCells.add(cell);
                    }
                }
            }

            // Re-render board to show post-gravity state + new candies
            view.renderBoard(board, this::handleCellClick);

            // Map gravity drops to panes (need lookup after re-render)
            Map<StackPane, Integer> dropDistances = new HashMap<>();
            for (Map.Entry<Cell, Integer> entry : gravityDrops.entrySet()) {
                StackPane pane = view.getCellPane(entry.getKey());
                if (pane != null) {
                    dropDistances.put(pane, entry.getValue());
                }
            }

            // Map new cells to panes with their row positions
            List<StackPane> spawnPanes = new ArrayList<>();
            Map<StackPane, Integer> spawnRows = new HashMap<>();
            for (Cell cell : newCells) {
                StackPane pane = view.getCellPane(cell);
                if (pane != null) {
                    spawnPanes.add(pane);
                    spawnRows.put(pane, cell.getRow());
                }
            }

            // Step 3: Animate gravity + spawn sequentially
            if (dropDistances.isEmpty() && spawnPanes.isEmpty()) {
                // Nothing to animate, proceed to next cascade step
                cascadeStep(board.findMatches());
            } else {
                animateGravityThenSpawn(dropDistances, spawnPanes, spawnRows);
            }
        });
    }

    /**
     * Animate gravity then spawn in sequence.
     */
    private void animateGravityThenSpawn(
            Map<StackPane, Integer> dropDistances,
            List<StackPane> spawnPanes,
            Map<StackPane, Integer> spawnRows) {

        if (!dropDistances.isEmpty()) {
            // Animate gravity first, then spawn
            animationManager.playGravity(dropDistances, () -> {
                if (!spawnPanes.isEmpty()) {
                    animationManager.playSpawn(spawnPanes, spawnRows, () -> {
                        // Check for next cascade matches
                        cascadeStep(board.findMatches());
                    });
                } else {
                    cascadeStep(board.findMatches());
                }
            });
        } else if (!spawnPanes.isEmpty()) {
            // Only spawn animation needed
            animationManager.playSpawn(spawnPanes, spawnRows, () -> {
                cascadeStep(board.findMatches());
            });
        }
    }

    private void unlockInput() {
        isAnimating.set(false);
        view.setAnimating(false);
    }

    private void transitionToGameOver() {
        state = gameOverState;
        state.onEnter();
        view.setStatusText("Game Over! Final Score: " + scoreManager.getScore());
    }

    private void restart() {
        board.reset();
        scoreManager.reset();
        state = playingState;
        selectedCell = null;
        isAnimating.set(false);
        view.setAnimating(false);
        view.setStatusText("");
        view.renderBoard(board, this::handleCellClick);
    }

    private void refreshView() {
        view.setScoreText("Score: " + scoreManager.getScore());
        view.setMovesText("Moves: " + scoreManager.getMoves());
        view.setStatusText("");
    }
}
