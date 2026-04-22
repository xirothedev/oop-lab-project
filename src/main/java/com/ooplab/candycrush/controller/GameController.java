package com.ooplab.candycrush.controller;

import com.ooplab.candycrush.model.*;
import com.ooplab.candycrush.view.GameView;

/**
 * Controller: handles player input, coordinates game logic between model and view.
 * Manages the full game flow: swap → match check → gravity → cascade → score → game over.
 */
public class GameController {
    private final Board board;
    private final ScoreManager scoreManager;
    private final GameView view;

    private GameState state;
    private final GameState playingState;
    private final GameState gameOverState;

    private Cell selectedCell;

    public GameController(Board board, ScoreManager scoreManager, GameView view) {
        this.board = board;
        this.scoreManager = scoreManager;
        this.view = view;

        this.playingState = new PlayingState();
        this.gameOverState = new GameOverState();
        this.state = playingState;

        setupView();
        refreshView();
        setupBindings();
    }

    /**
     * Set up view event handlers and bindings.
     */
    private void setupView() {
        // Cell click handler
        view.renderBoard(board, this::handleCellClick);

        // Restart button
        view.setOnRestart(this::restart);

        // Bind observable properties for auto-updating UI (Observer Pattern)
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

    /**
     * Handle cell click: select first cell, then attempt swap with second.
     */
    private void handleCellClick(Cell clicked) {
        if (!state.canPlay()) {
            return;
        }

        if (selectedCell == null) {
            // First selection
            selectedCell = clicked;
            view.highlightCell(clicked);
            view.setStatusText("");
        } else {
            // Second selection: attempt swap
            Cell first = selectedCell;
            selectedCell = null;

            if (first == clicked) {
                // Deselect
                view.renderBoard(board, this::handleCellClick);
                view.setStatusText("");
                return;
            }

            if (isAdjacent(first, clicked)) {
                attemptSwap(first, clicked);
            } else {
                // Select new cell instead
                selectedCell = clicked;
                view.highlightCell(clicked);
                view.setStatusText("");
            }
        }
    }

    /**
     * Check if two cells are adjacent (up/down/left/right).
     */
    private boolean isAdjacent(Cell a, Cell b) {
        int rowDiff = Math.abs(a.getRow() - b.getRow());
        int colDiff = Math.abs(a.getCol() - b.getCol());
        return (rowDiff + colDiff) == 1;
    }

    /**
     * Attempt a swap, check for matches, and handle cascade.
     */
    private void attemptSwap(Cell a, Cell b) {
        // Perform swap
        board.swap(a, b);

        // Check for matches
        var matches = board.findMatches();

        if (matches.isEmpty()) {
            // No match — revert swap
            board.swap(a, b);
            view.setStatusText("Invalid move!");
            view.renderBoard(board, this::handleCellClick);
            return;
        }

        // Valid match — process cascade
        processCascade(matches);

        // Use a move
        scoreManager.useMove();

        // Check game over
        if (scoreManager.isGameOver()) {
            transitionToGameOver();
        }

        // Refresh view
        view.renderBoard(board, this::handleCellClick);
    }

    /**
     * Process cascade: remove matches, apply gravity, refill, repeat until no matches.
     */
    private void processCascade(java.util.Set<Cell> matches) {
        while (!matches.isEmpty()) {
            int count = board.removeMatches(matches);
            scoreManager.addScore(count);

            board.applyGravity();
            board.fillEmpty();

            matches = board.findMatches();
        }
    }

    /**
     * Transition to Game Over state.
     */
    private void transitionToGameOver() {
        state = gameOverState;
        state.onEnter();
        view.setStatusText("Game Over! Final Score: " + scoreManager.getScore());
    }

    /**
     * Restart the game: new board, reset score, back to playing state.
     */
    private void restart() {
        // Reset all state
        board.reset();
        scoreManager.reset();
        state = playingState;
        selectedCell = null;
        view.setStatusText("");
        view.renderBoard(board, this::handleCellClick);
    }

    /**
     * Initial view refresh.
     */
    private void refreshView() {
        view.setScoreText("Score: " + scoreManager.getScore());
        view.setMovesText("Moves: " + scoreManager.getMoves());
        view.setStatusText("");
    }
}
