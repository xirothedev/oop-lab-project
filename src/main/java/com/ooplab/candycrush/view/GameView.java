package com.ooplab.candycrush.view;

import com.ooplab.candycrush.model.Board;
import com.ooplab.candycrush.model.Cell;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * JavaFX view: renders the 8x8 board grid, score, moves, status, and restart button.
 */
public class GameView {
    private static final int CELL_SIZE = 60;
    private static final int GAP = 3;

    private final Stage stage;
    private final BorderPane root;
    private final GridPane boardGrid;
    private final Label scoreLabel;
    private final Label movesLabel;
    private final Label statusLabel;
    private final Button restartButton;

    // Track selected cell highlight
    private Rectangle selectedHighlight;

    public GameView(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.boardGrid = new GridPane();
        this.boardGrid.setAlignment(Pos.CENTER);
        this.boardGrid.setHgap(GAP);
        this.boardGrid.setVgap(GAP);
        this.boardGrid.setPadding(new Insets(10));

        // Info panel (top)
        VBox infoPanel = new VBox(8);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setPadding(new Insets(15));

        Label titleLabel = new Label("Candy Crush");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        movesLabel = new Label("Moves: 20");
        movesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        statsBox.getChildren().addAll(scoreLabel, movesLabel);

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.RED);

        restartButton = new Button("Restart");
        restartButton.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        restartButton.setPrefWidth(120);

        infoPanel.getChildren().addAll(titleLabel, statsBox, statusLabel, restartButton);

        root.setTop(infoPanel);
        root.setCenter(boardGrid);

        Scene scene = new Scene(root, 600, 680);
        scene.setFill(Color.LAVENDER);
        stage.setTitle("Candy Crush - OOP Project");
        stage.setScene(scene);
        stage.setResizable(false);
    }

    public void show() {
        stage.show();
    }

    /**
     * Build the board grid UI from the model Board.
     */
    public void renderBoard(Board board, Consumer<Cell> onCellClick) {
        boardGrid.getChildren().clear();
        selectedHighlight = null;

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Cell cell = board.getCell(r, c);
                StackPane pane = createCellPane(cell);
                pane.setOnMouseClicked(e -> onCellClick.accept(cell));
                pane.setUserData(cell);
                boardGrid.add(pane, c, r);
            }
        }
    }

    /**
     * Create a visual representation of a cell.
     */
    private StackPane createCellPane(Cell cell) {
        StackPane pane = new StackPane();
        pane.setPrefSize(CELL_SIZE, CELL_SIZE);

        Rectangle bg = new Rectangle(CELL_SIZE - 4, CELL_SIZE - 4);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.LIGHTGRAY);

        pane.getChildren().add(bg);

        if (!cell.isEmpty() && cell.getCandy() != null) {
            Rectangle candyRect = new Rectangle(CELL_SIZE - 16, CELL_SIZE - 16);
            candyRect.setArcWidth(8);
            candyRect.setArcHeight(8);
            candyRect.setFill(cell.getCandy().getType().getColor());
            candyRect.setStroke(Color.DARKGRAY);
            candyRect.setStrokeWidth(1);
            pane.getChildren().add(candyRect);
        }

        return pane;
    }

    /**
     * Highlight a selected cell.
     */
    public void highlightCell(Cell cell) {
        // Remove previous highlight
        boardGrid.getChildren().forEach(node -> {
            StackPane pane = (StackPane) node;
            // Remove highlight rectangle if present
            pane.getChildren().removeIf(n -> n instanceof Rectangle r && r.getStroke() == Color.GOLD && r.getStrokeWidth() == 3);
        });

        // Add highlight to new selection
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            StackPane pane = (StackPane) node;
            Cell c = (Cell) pane.getUserData();
            if (c == cell) {
                Rectangle highlight = new Rectangle(CELL_SIZE - 4, CELL_SIZE - 4);
                highlight.setArcWidth(10);
                highlight.setArcHeight(10);
                highlight.setFill(Color.TRANSPARENT);
                highlight.setStroke(Color.GOLD);
                highlight.setStrokeWidth(3);
                pane.getChildren().add(highlight);
                selectedHighlight = highlight;
                break;
            }
        }
    }

    /**
     * Bind score label to ScoreManager property (Observer Pattern).
     */
    public void bindScore(javafx.beans.binding.IntegerBinding scoreBinding) {
        scoreLabel.textProperty().bind(
                scoreBinding.asString().concat(" Score: ").concat(
                        new javafx.beans.binding.StringBinding() {
                            { bind(scoreBinding); }
                            @Override protected String computeValue() { return ""; }
                        }
                )
        );
        // Simpler approach: just bind directly
        // Redone below — use setScoreText from controller instead
    }

    /**
     * Update score display.
     */
    public void setScoreText(String text) {
        scoreLabel.setText(text);
    }

    /**
     * Update moves display.
     */
    public void setMovesText(String text) {
        movesLabel.setText(text);
    }

    /**
     * Update status message.
     */
    public void setStatusText(String text) {
        statusLabel.setText(text);
    }

    /**
     * Set restart button action.
     */
    public void setOnRestart(Runnable action) {
        restartButton.setOnAction(e -> action.run());
    }

    public Stage getStage() {
        return stage;
    }
}
