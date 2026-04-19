package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.app.GameSession;
import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.GameStatus;
import com.ooplab.candycrush.domain.GoalType;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.Position;
import com.ooplab.candycrush.domain.ResolutionResult;
import com.ooplab.candycrush.domain.SpecialType;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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

    private LevelDefinition level;
    private Position selectedPosition;

    public void setLevel(LevelDefinition level) {
        this.level = level;
        GameSession session = router().context().gameSession();
        session.start(level);
        updateHeader();
        renderBoard();
    }

    @FXML
    private void handleRestart() {
        router().context().gameSession().restart();
        selectedPosition = null;
        updateHeader();
        renderBoard();
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

    private void renderBoard() {
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
        if (selectedPosition == null) {
            selectedPosition = position;
            updateHeader();
            renderBoard();
            return;
        }
        if (selectedPosition.equals(position)) {
            selectedPosition = null;
            updateHeader();
            renderBoard();
            return;
        }

        GameSession session = router().context().gameSession();
        ResolutionResult result = session.applyMove(new Move(selectedPosition, position));
        selectedPosition = null;
        if (!result.accepted()) {
            hintLabel.setText("Invalid move. Try another swap.");
        }
        updateHeader();
        renderBoard();
        if (result.endState() == GameStatus.WON || result.endState() == GameStatus.LOST) {
            if (result.endState() == GameStatus.WON) {
                router().context().leaderboardRepository().addScore("Player", session.getState().score(), session.getLevel().name());
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(result.endState() == GameStatus.WON ? "Level completed" : "Out of moves");
            alert.setContentText("Final score: " + session.getState().score());
            alert.showAndWait();
        }
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

