package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.app.GameSession;
import com.ooplab.candycrush.domain.*;
import com.ooplab.candycrush.ui.animation.AnimationService;
import com.ooplab.candycrush.ui.animation.CandyNodeManager;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class GameController extends BaseController {
    private static final Logger LOG = Logger.getLogger(GameController.class.getName());
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
    @FXML
    private Button hintButton;
    @FXML
    private Region goalProgressBg;
    @FXML
    private Region goalProgressFill;

    private Position selectedPosition;
    private Position hintFirst;
    private Position hintSecond;
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
        hintFirst = null;
        hintSecond = null;
        clearHintHighlight();
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

    @FXML
    private void handleHint() {
        GameSession session = router().context().gameSession();
        Optional<Move> hint = session.findHint();
        if (hint.isPresent()) {
            Move move = hint.get();
            hintFirst = move.first();
            hintSecond = move.second();
            applyHintHighlight();
            hintLabel.setText("Hint: swap the highlighted candies.");
        }
    }

    private void clearHintHighlight() {
        for (int row = 0; row < boardGrid.getRowCount(); row++) {
            for (int col = 0; col < boardGrid.getColumnCount(); col++) {
                var cell = getCellAt(col, row);
                if (cell != null) {
                    cell.getStyleClass().removeAll("hint-cell");
                }
            }
        }
    }

    private void applyHintHighlight() {
        clearHintHighlight();
        highlightCell(hintFirst);
        highlightCell(hintSecond);
    }

    private void highlightCell(Position position) {
        if (position == null) return;
        for (int row = 0; row < boardGrid.getRowCount(); row++) {
            for (int col = 0; col < boardGrid.getColumnCount(); col++) {
                var cell = getCellAt(col, row);
                if (cell != null && row == position.row() && col == position.col()) {
                    cell.getStyleClass().add("hint-cell");
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(300), cell);
                    pulse.setFromX(1.0);
                    pulse.setToX(1.1);
                    pulse.setFromY(1.0);
                    pulse.setToY(1.1);
                    pulse.setAutoReverse(true);
                    pulse.setCycleCount(2);
                    pulse.play();
                }
            }
        }
    }

    private void updateHeader() {
        GameSession session = router().context().gameSession();
        LevelDefinition level = session.getLevel();
        levelNameLabel.setText(level.name());
        scoreLabel.setText(String.valueOf(session.getState().score()));
        movesLabel.setText(String.valueOf(session.getState().movesLeft()));
        if (level.goalType() == GoalType.TARGET_SCORE) {
            goalLabel.setText(String.valueOf(level.targetScore()));
        } else {
            goalLabel.setText(session.getBoard().jellyCount() + " left");
        }
        updateGoalProgress();
        hintButton.setVisible(session.getState().status() == GameStatus.RUNNING);
        hintLabel.setText(selectedPosition == null
                ? "Select two adjacent candies to swap."
                : "Selected: (" + selectedPosition.row() + ", " + selectedPosition.col() + ")");
    }

    private void updateGoalProgress() {
        GameSession session = router().context().gameSession();
        LevelDefinition level = session.getLevel();
        double progress = 0.0;
        if (level.goalType() == GoalType.TARGET_SCORE && level.targetScore() > 0) {
            progress = Math.min(1.0, (double) session.getState().score() / level.targetScore());
        } else if (level.goalType() == GoalType.CLEAR_JELLY) {
            int totalJelly = level.jellyCells().size();
            if (totalJelly > 0) {
                progress = 1.0 - (double) session.getBoard().jellyCount() / totalJelly;
            }
        }
        double width = Math.max(0, progress * goalProgressBg.getWidth());
        goalProgressFill.setMaxWidth(Math.max(0, width));
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
        goalProgressBg.widthProperty().addListener((obs, old, nw) -> updateGoalProgress());
    }

    private StackPane createCell(Board board, Position position) {
        Candy candy = board.getCandy(position);
        StackPane cell = new StackPane();
        cell.getStyleClass().add("board-cell");
        if (position.equals(selectedPosition)) {
            cell.getStyleClass().add("selected-cell");
        }

        Pane candyNode = createCandyVisual(candy);
        if (candyNode != null) {
            cell.getChildren().add(candyNode);
        }

        if (board.hasJelly(position)) {
            Region jellyOverlay = new Region();
            jellyOverlay.getStyleClass().add("jelly-overlay");
            cell.getChildren().add(jellyOverlay);
        }

        cell.setOnMouseClicked(event -> handleCellClick(position));
        return cell;
    }

    private Pane createCandyVisual(Candy candy) {
        if (candy == null) return null;

        Region base = new Region();
        base.setMinSize(40, 40);
        base.setPrefSize(40, 40);
        base.setMaxSize(40, 40);
        base.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(candyBg(candy),
                        new javafx.scene.layout.CornerRadii(14), null)));

        Pane container = new StackPane();
        container.getChildren().add(base);

        if (candy.specialType() == SpecialType.STRIPED_ROW) {
            Region stripe = new Region();
            stripe.setMinSize(36, 3);
            stripe.setBackground(new javafx.scene.layout.Background(
                    new javafx.scene.layout.BackgroundFill(Color.web("rgba(255,255,255,0.65)"),
                            new javafx.scene.layout.CornerRadii(2), null)));
            container.getChildren().add(stripe);
        } else if (candy.specialType() == SpecialType.STRIPED_COLUMN) {
            Region stripe = new Region();
            stripe.setMinSize(3, 36);
            stripe.setBackground(new javafx.scene.layout.Background(
                    new javafx.scene.layout.BackgroundFill(Color.web("rgba(255,255,255,0.65)"),
                            new javafx.scene.layout.CornerRadii(2), null)));
            container.getChildren().add(stripe);
        } else if (candy.specialType() == SpecialType.WRAPPED) {
            Region border = new Region();
            border.setMinSize(40, 40);
            border.setBorder(new javafx.scene.layout.Border(
                    new javafx.scene.layout.BorderStroke(Color.web("#ffd43b"),
                            javafx.scene.layout.BorderStrokeStyle.SOLID,
                            new javafx.scene.layout.CornerRadii(14),
                            new javafx.scene.layout.BorderWidths(3))));
            container.getChildren().add(border);
        }

        return container;
    }

    private Color candyBg(Candy candy) {
        if (candy.specialType() == SpecialType.COLOR_BOMB) {
            return Color.web("#2d3436");
        }
        if (candy.color() == null) return Color.web("#636e72");
        return switch (candy.color()) {
            case RED -> Color.web("#ff6b6b");
            case GREEN -> Color.web("#51cf66");
            case BLUE -> Color.web("#4dabf7");
            case YELLOW -> Color.web("#ffd43b");
            case ORANGE -> Color.web("#ffa94d");
            case PURPLE -> Color.web("#b197fc");
        };
    }

    private void handleCellClick(Position position) {
        if (animationService.isAnimating()) {
            LOG.info("Click ignored — animation in progress");
            return;
        }

        LOG.info(() -> "Click: (" + position.row() + "," + position.col() + ")");

        clearHintHighlight();
        hintFirst = null;
        hintSecond = null;

        if (selectedPosition == null) {
            selectedPosition = position;
            LOG.info(() -> "Selected: (" + position.row() + "," + position.col() + ")");
            updateHeader();
            refreshCellSelection();
            return;
        }
        if (selectedPosition.equals(position)) {
            selectedPosition = null;
            LOG.info("Deselected");
            updateHeader();
            refreshCellSelection();
            return;
        }

        GameSession session = router().context().gameSession();
        Board preSnapshot = session.getBoardCopy();
        Position first = selectedPosition;
        selectedPosition = null;
        updateHeader();

        LOG.info(() -> "Swap: (" + first.row() + "," + first.col() + ") <-> (" + position.row() + "," + position.col() + ")");

        animationService.playSwap(first, position, () -> {
            ResolutionResult result = session.applyMove(new Move(first, position));

            LOG.info(() -> "Result: accepted=" + result.accepted() + " score=" + result.scoreDelta() + " events=" + result.events().size() + " endState=" + result.endState());

            if (!result.accepted()) {
                LOG.info("Reverse swap — invalid move");
                animationService.playReverseSwap(first, position, () -> refreshCellSelection());
                hintLabel.setText("Invalid move. Try another swap.");
                return;
            }

            Map<Position, Candy> refilled = findNewCandies(preSnapshot, session.getBoard());
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
                showGameEndOverlay(result.endState(), session.getState().score(), session.getLevel());
            }
        });
    }

    private void showGameEndOverlay(GameStatus status, int score, LevelDefinition level) {
        overlayPane.setMouseTransparent(false);

        VBox panel = new VBox(16);
        panel.getStyleClass().add("game-end-panel");
        panel.setAlignment(Pos.CENTER);

        Label title = new Label(status == GameStatus.WON ? "Level Complete!" : "Out of Moves");
        title.getStyleClass().add(status == GameStatus.WON ? "game-end-title" : "game-end-title-lose");

        Label scoreText = new Label("Final Score: " + score);
        scoreText.getStyleClass().add("game-end-score");

        Label stars = new Label(calculateStars(score, level));
        stars.getStyleClass().add("star-label");

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);
        Button retryBtn = new Button("Retry");
        retryBtn.getStyleClass().add("button-secondary");
        retryBtn.setOnAction(e -> {
            overlayPane.getChildren().clear();
            overlayPane.setMouseTransparent(true);
            handleRestart();
        });
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            overlayPane.getChildren().clear();
            overlayPane.setMouseTransparent(true);
            handleBack();
        });
        buttons.getChildren().addAll(retryBtn, backBtn);

        panel.getChildren().addAll(title, stars, scoreText, buttons);

        StackPane wrapper = new StackPane(panel);
        wrapper.getStyleClass().add("game-end-overlay");
        wrapper.setOnMouseClicked(e -> {});

        overlayPane.getChildren().add(wrapper);

        ScaleTransition scale = new ScaleTransition(Duration.millis(400), panel);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private String calculateStars(int score, LevelDefinition level) {
        if (level.goalType() == GoalType.TARGET_SCORE && level.targetScore() > 0) {
            double ratio = (double) score / level.targetScore();
            if (ratio >= 2.0) return "★★★";
            if (ratio >= 1.5) return "★★☆";
            return "★☆☆";
        }
        return "★★★";
    }

    private Map<Position, Candy> findNewCandies(Board before, Board after) {
        Map<Position, Candy> newCandies = new HashMap<>();
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
}
