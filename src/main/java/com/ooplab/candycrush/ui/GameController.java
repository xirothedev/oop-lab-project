package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.app.GameSession;
import com.ooplab.candycrush.domain.*;
import com.ooplab.candycrush.ui.animation.AnimationService;
import com.ooplab.candycrush.ui.animation.CandyNodeManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class GameController extends BaseController {
    @FXML
    private Label levelNameLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label movesLabel;
    @FXML
    private Label goalLabel;
    @FXML
    private Label hintLabel;
    @FXML
    private GridPane boardGrid;
    @FXML
    private Pane overlayPane;

    private Position selectedPosition;
    private CandyNodeManager nodeManager;
    private AnimationService animationService;

    public void setLevel(LevelDefinition level) {
        GameSession session = router().context().gameSession();
        session.start(level);
        nodeManager = new CandyNodeManager();
        animationService = new AnimationService(boardGrid, overlayPane, nodeManager);
        updateHeader();
        initializeBoard();
    }

    @FXML
    private void handleRestart() {
        router().context().gameSession().restart();
        selectedPosition = null;
        nodeManager.clear();
        boardGrid.getChildren().clear();
        overlayPane.getChildren().clear();
        updateHeader();
        initializeBoard();
    }

    @FXML
    private void handleBack() {
        router().showLevelSelect();
    }

    private void updateHeader() {
        GameSession session = router().context().gameSession();
        levelNameLabel.setText(session.getLevel().name());
        scoreLabel.setText("Score: " + session.getState().score());
        movesLabel.setText("Moves: " + session.getState().movesLeft());
        if (session.getLevel().goalType() == GoalType.TARGET_SCORE) {
            goalLabel.setText("Goal: Reach " + session.getLevel().targetScore());
        } else {
            goalLabel.setText("Goal: Clear " + session.getBoard().jellyCount() + " jelly");
        }
        hintLabel.setText(selectedPosition == null
                ? "Select two adjacent candies to swap."
                : "Selected: (" + selectedPosition.row() + ", " + selectedPosition.col() + ")");
    }

    private void initializeBoard() {
        boardGrid.getChildren().clear();
        boardGrid.setHgap(6);
        boardGrid.setVgap(6);
        boardGrid.setPadding(new Insets(8));
        Board board = router().context().gameSession().getBoard();
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position position = new Position(row, col);
                StackPane cell = createCell(board, position);
                boardGrid.add(cell, col, row);
                nodeManager.register(position, cell);
            }
        }
    }

    private StackPane createCell(Board board, Position position) {
        Candy candy = board.getCandy(position);
        StackPane cell = new StackPane();
        cell.getStyleClass().add("board-cell");
        if (position.equals(selectedPosition)) {
            cell.getStyleClass().add("selected-cell");
        }
        if (board.hasJelly(position)) {
            cell.getStyleClass().add("jelly-cell");
        }

        Circle circle = new Circle(22);
        circle.setFill(colorOf(candy));
        circle.setStroke(Color.rgb(255, 255, 255, 0.75));
        circle.setStrokeWidth(2);
        Label label = new Label(symbolOf(candy));
        label.getStyleClass().add("candy-symbol");
        cell.getChildren().addAll(circle, label);
        cell.setOnMouseClicked(event -> handleCellClick(position));
        return cell;
    }

    private void handleCellClick(Position position) {
        if (animationService.isAnimating()) return;

        if (selectedPosition == null) {
            selectedPosition = position;
            updateHeader();
            refreshCellSelection();
            return;
        }
        if (selectedPosition.equals(position)) {
            selectedPosition = null;
            updateHeader();
            refreshCellSelection();
            return;
        }

        GameSession session = router().context().gameSession();
        Board preSnapshot = session.getBoardCopy();
        Position first = selectedPosition;
        selectedPosition = null;
        updateHeader();

        animationService.playSwap(first, position, () -> {
            ResolutionResult result = session.applyMove(new Move(first, position));

            if (!result.accepted()) {
                animationService.playReverseSwap(first, position, () -> refreshCellSelection());
                hintLabel.setText("Invalid move. Try another swap.");
                return;
            }

            java.util.Map<Position, Candy> refilled = findNewCandies(preSnapshot, session.getBoard());
            animationService.playEvents(result.events(), preSnapshot, session.getBoard(), () -> {
                if (!refilled.isEmpty()) {
                    animationService.playRefill(refilled, this::refreshBoard);
                } else {
                    refreshBoard();
                }
            });

            if (result.endState() == GameStatus.WON || result.endState() == GameStatus.LOST) {
                if (result.endState() == GameStatus.WON) {
                    router().context().leaderboardRepository().addScore("Player", session.getState().score(), session.getLevel().name());
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(result.endState() == GameStatus.WON ? "Level completed" : "Out of moves");
                alert.setContentText("Final score: " + session.getState().score());
                alert.showAndWait();
                refreshBoard();
            }
        });
    }

    private java.util.Map<Position, Candy> findNewCandies(Board before, Board after) {
        java.util.Map<Position, Candy> newCandies = new java.util.HashMap<>();
        for (int row = 0; row < after.rows(); row++) {
            for (int col = 0; col < after.cols(); col++) {
                Position pos = new Position(row, col);
                Candy afterCandy = after.getCandy(pos);
                Candy beforeCandy = before.getCandy(pos);
                if (afterCandy != null && beforeCandy == null) {
                    newCandies.put(pos, afterCandy);
                }
            }
        }
        return newCandies;
    }

    private void refreshCellSelection() {
        for (int row = 0; row < boardGrid.getRowCount(); row++) {
            for (int col = 0; col < boardGrid.getColumnCount(); col++) {
                var cell = getCellAt(col, row);
                if (cell != null) {
                    cell.getStyleClass().removeAll("selected-cell");
                    if (selectedPosition != null && selectedPosition.row() == row && selectedPosition.col() == col) {
                        cell.getStyleClass().add("selected-cell");
                    }
                }
            }
        }
    }

    private StackPane getCellAt(int col, int row) {
        for (var node : boardGrid.getChildren()) {
            if (node instanceof StackPane cell) {
                Integer cellCol = GridPane.getColumnIndex(cell);
                Integer cellRow = GridPane.getRowIndex(cell);
                if ((cellCol == null ? 0 : cellCol) == col && (cellRow == null ? 0 : cellRow) == row) {
                    return cell;
                }
            }
        }
        return null;
    }

    private void refreshBoard() {
        nodeManager.clear();
        overlayPane.getChildren().clear();
        boardGrid.getChildren().clear();
        initializeBoard();
        updateHeader();
    }

    private Color colorOf(Candy candy) {
        if (candy == null || candy.specialType() == SpecialType.COLOR_BOMB) {
            return Color.web("#2d3436");
        }
        CandyColor color = candy.color();
        return switch (color) {
            case RED -> Color.web("#ff6b6b");
            case GREEN -> Color.web("#51cf66");
            case BLUE -> Color.web("#4dabf7");
            case YELLOW -> Color.web("#ffd43b");
            case ORANGE -> Color.web("#ffa94d");
            case PURPLE -> Color.web("#b197fc");
        };
    }

    private String symbolOf(Candy candy) {
        if (candy == null) {
            return "";
        }
        return switch (candy.specialType()) {
            case STRIPED_ROW -> "H";
            case STRIPED_COLUMN -> "V";
            case WRAPPED -> "W";
            case COLOR_BOMB -> "B";
            case NONE -> "";
        };
    }
}
