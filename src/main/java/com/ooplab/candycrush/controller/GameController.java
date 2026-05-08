package com.ooplab.candycrush.controller;

import com.ooplab.candycrush.model.*;
import com.ooplab.candycrush.util.AnimationManager;
import com.ooplab.candycrush.util.JavaFXAnimationManager;
import com.ooplab.candycrush.view.GameView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private GameState state;
    private final GameState playingState;
    private final GameState gameOverState;

    private Cell selectedCell;
    private final AtomicBoolean isAnimating = new AtomicBoolean(false);

    public GameController(Board board, ScoreManager scoreManager, GameView view) {
        this(board, scoreManager, view, new JavaFXAnimationManager());
    }

    public GameController(Board board, ScoreManager scoreManager, GameView view, AnimationManager animationManager) {
        this.board = board;
        this.scoreManager = scoreManager;
        this.view = view;
        this.animationManager = animationManager;

        this.playingState = new PlayingState(view);
        this.gameOverState = new GameOverState(view, scoreManager);
        this.state = playingState;

        setupView();
        setupBindings();
        refreshView();
    }

    private void setupView() {
        view.renderBoard(board, this::handleCellClick);
        view.setOnRestart(this::restart);
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
            return;
        }

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

    private boolean isAdjacent(Cell a, Cell b) {
        int rowDiff = Math.abs(a.getRow() - b.getRow());
        int colDiff = Math.abs(a.getCol() - b.getCol());
        return (rowDiff + colDiff) == 1;
    }

    private void attemptSwap(Cell a, Cell b) {
        isAnimating.set(true);
        view.setAnimating(true);

        StackPane paneA = view.getCellPane(a);
        StackPane paneB = view.getCellPane(b);

        animationManager.playSwap(paneA, paneB, () -> resolveSwap(a, b, paneA, paneB));
    }

    private void resolveSwap(Cell a, Cell b, StackPane paneA, StackPane paneB) {
        board.swap(a, b);
        MatchResolution resolution = board.resolveMatches(a, b, true);

        if (resolution.isEmpty()) {
            board.swap(a, b);
            animationManager.playSwapBack(paneA, paneB, this::onInvalidSwap);
            return;
        }

        cascadeStep(resolution);
    }

    private void onInvalidSwap() {
        view.renderBoard(board, this::handleCellClick);
        view.setStatusText("Invalid move!");
        unlockInput();
    }

    /**
     * One step of the cascade. The chain is:
     *   removal animation → apply model changes → gravity animation → spawn animation → next cascade step.
     * Each phase is a small private method so the flow reads top-to-bottom rather than nesting.
     */
    private void cascadeStep(MatchResolution resolution) {
        if (resolution.isEmpty()) {
            finishCascade();
            return;
        }
        animationManager.playRemoval(collectPanes(resolution.clearedCells()),
                () -> applyResolutionAndContinue(resolution));
    }

    private void finishCascade() {
        scoreManager.useMove();
        view.renderBoard(board, this::handleCellClick);
        unlockInput();

        if (scoreManager.isGameOver()) {
            transitionTo(gameOverState);
        }
    }

    private void applyResolutionAndContinue(MatchResolution resolution) {
        board.applyMatchResolution(resolution);
        scoreManager.addScore(resolution.clearedCells().size() + resolution.specialSpawns().size());

        Map<Cell, Integer> gravityDrops = board.applyGravity();
        List<Cell> newCells = collectEmptyCells();
        board.fillEmpty();

        view.renderBoard(board, this::handleCellClick);

        Map<StackPane, Integer> dropDistances = mapDropPanes(gravityDrops);
        List<StackPane> spawnPanes = new ArrayList<>();
        Map<StackPane, Integer> spawnRows = new HashMap<>();
        mapSpawnPanes(newCells, spawnPanes, spawnRows);

        MatchResolution next = board.resolveMatches(null, null, false);
        runGravityThenSpawn(dropDistances, spawnPanes, spawnRows, () -> cascadeStep(next));
    }

    private void runGravityThenSpawn(
            Map<StackPane, Integer> dropDistances,
            List<StackPane> spawnPanes,
            Map<StackPane, Integer> spawnRows,
            Runnable next) {
        Runnable spawnPhase = () -> {
            if (spawnPanes.isEmpty()) {
                next.run();
            } else {
                animationManager.playSpawn(spawnPanes, spawnRows, next);
            }
        };

        if (dropDistances.isEmpty()) {
            spawnPhase.run();
        } else {
            animationManager.playGravity(dropDistances, spawnPhase);
        }
    }

    private List<StackPane> collectPanes(Collection<Cell> cells) {
        List<StackPane> panes = new ArrayList<>();
        for (Cell cell : cells) {
            StackPane pane = view.getCellPane(cell);
            if (pane != null) {
                panes.add(pane);
            }
        }
        return panes;
    }

    private List<Cell> collectEmptyCells() {
        List<Cell> empties = new ArrayList<>();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isEmpty()) {
                    empties.add(cell);
                }
            }
        }
        return empties;
    }

    private Map<StackPane, Integer> mapDropPanes(Map<Cell, Integer> gravityDrops) {
        Map<StackPane, Integer> dropDistances = new HashMap<>();
        for (Map.Entry<Cell, Integer> entry : gravityDrops.entrySet()) {
            StackPane pane = view.getCellPane(entry.getKey());
            if (pane != null) {
                dropDistances.put(pane, entry.getValue());
            }
        }
        return dropDistances;
    }

    private void mapSpawnPanes(List<Cell> newCells, List<StackPane> spawnPanes, Map<StackPane, Integer> spawnRows) {
        for (Cell cell : newCells) {
            StackPane pane = view.getCellPane(cell);
            if (pane != null) {
                spawnPanes.add(pane);
                spawnRows.put(pane, cell.getRow());
            }
        }
    }

    private void unlockInput() {
        isAnimating.set(false);
        view.setAnimating(false);
    }

    private void transitionTo(GameState newState) {
        state = newState;
        state.onEnter();
    }

    private void restart() {
        board.reset();
        scoreManager.reset();
        selectedCell = null;
        isAnimating.set(false);
        view.setAnimating(false);
        view.renderBoard(board, this::handleCellClick);
        transitionTo(playingState);
    }

    private void refreshView() {
        view.setScoreText("Score: " + scoreManager.getScore());
        view.setMovesText("Moves: " + scoreManager.getMoves());
        view.setStatusText("");
    }
}
