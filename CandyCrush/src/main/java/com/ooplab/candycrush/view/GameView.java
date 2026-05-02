package com.ooplab.candycrush.view;

import com.ooplab.candycrush.model.Board;
import com.ooplab.candycrush.model.Cell;
import com.ooplab.candycrush.util.SoundManager;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.*;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.beans.binding.DoubleBinding;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX view: renders the 8x8 board grid, score, moves, status, and restart button.
 */
public class GameView {
    private static final int CELL_SIZE = 70;
    private static final int GAP = 3;
    private final Stage stage;
    private final StackPane root;
    private final GridPane boardGrid;
    private DoubleBinding cellSizeBinding;
    private final Label scoreLabel;
    private final Label movesLabel;
    private final Label statusLabel;
    private final Button restartButton;

    // Track selected cell highlight
    private Rectangle selectedHighlight;

    // Map Cell  StackPane for animation targeting
    private final Map<Cell, StackPane> cellMap = new HashMap<>();

    private boolean isAnimating = false;

    public GameView(Stage stage) {
        this.stage = stage;
        BorderPane mainLayout = new BorderPane();
        this.root = new StackPane(mainLayout);
        this.boardGrid = new GridPane();
        cellSizeBinding = new DoubleBinding() {
            {
                bind(boardGrid.widthProperty(), boardGrid.heightProperty());
            }

            @Override
            protected double computeValue() {
                double size = Math.min(boardGrid.getWidth(), boardGrid.getHeight());
                return size / Board.SIZE;
            }
        };
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
        restartButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        restartButton.setPrefWidth(120);
        restartButton.setPrefHeight(40);

        restartButton.setStyle(
                "-fx-background-color: #ff3b3b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );

        restartButton.setOnMouseEntered(e ->
                restartButton.setStyle(
                        "-fx-background-color: #ff5c5c;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 20;" +
                                "-fx-cursor: hand;"
                )
        );

        restartButton.setOnMousePressed(e -> {
            restartButton.setStyle(
                    "-fx-background-color: #cc0000;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 20;"
            );
            restartButton.setScaleX(0.95);
            restartButton.setScaleY(0.95);
        });

        restartButton.setOnMouseReleased(e -> {
            restartButton.setStyle(
                    "-fx-background-color: #ff3b3b;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 20;"
            );
            restartButton.setScaleX(1);
            restartButton.setScaleY(1);
        });

        infoPanel.getChildren().addAll(titleLabel, statsBox, statusLabel, restartButton);
        //Background
        mainLayout.setTop(infoPanel);
        root.setStyle(
                "-fx-background-image: url('/images/bg.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: 50% right;"
        );
        mainLayout.setCenter(boardGrid);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, 600, 680);
        scene.setFill(Color.LAVENDER);
        stage.setTitle("Candy Crush - OOP Project");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setMaximized(true);
        stage.setFullScreen(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

    }
    public void showComboAt(Cell cell, String text) {
        StackPane pane = cellMap.get(cell);
        if (pane == null) return;

        Label combo = new Label(text);
        combo.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-text-fill: yellow;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, black, 5, 0.5, 0, 0);"
        );


        Bounds bounds = pane.localToScene(pane.getBoundsInLocal());
        Point2D point = root.sceneToLocal(bounds.getMinX(), bounds.getMinY());

        combo.setLayoutX(point.getX() + pane.getWidth() / 2 - 30);
        combo.setLayoutY(point.getY() + pane.getHeight() / 2 - 10);

        root.getChildren().add(combo);

        ScaleTransition scale = new ScaleTransition(Duration.millis(250), combo);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.2);
        scale.setToY(1.2);

        TranslateTransition move = new TranslateTransition(Duration.millis(2000), combo);
        move.setByY(-30);

        FadeTransition fade = new FadeTransition(Duration.millis(700), combo);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition anim = new ParallelTransition(scale, move, fade);
        anim.setOnFinished(e -> root.getChildren().remove(combo));
        anim.play();
    }

    public void show() {
        stage.show();
    }

    /**
     * Build the board grid UI from the model Board.
     */
    public void renderBoard(Board board, Consumer<Cell> onCellClick) {
        boardGrid.getChildren().clear();
        cellMap.clear();
        selectedHighlight = null;

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Cell cell = board.getCell(r, c);
                StackPane pane = createCellPane(cell);
                pane.setOnMouseClicked(e -> onCellClick.accept(cell));
                pane.setUserData(cell);
                boardGrid.add(pane, c, r);
                cellMap.put(cell, pane);
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
        bg.setFill(Color.TRANSPARENT);
        bg.setStroke(null);

        pane.getChildren().add(bg);

        if (!cell.isEmpty() && cell.getCandy() != null) {
            Image img = new Image(getClass().getResourceAsStream(
                    cell.getCandy().getType().getImagePath()
            ));

            ImageView iv = new ImageView(img);
            iv.setFitWidth(CELL_SIZE - 16);
            iv.setFitHeight(CELL_SIZE - 16);
            iv.setPreserveRatio(true);

            pane.getChildren().add(iv);
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

        if (cell == null) {
            return;
        }

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
        restartButton.setOnAction(e -> {SoundManager.playClick();action.run();});
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * Get the StackPane for a given cell.
     */
    public StackPane getCellPane(Cell cell) {
        return cellMap.get(cell);
    }

    /**
     * Get the underlying GridPane.
     */
    public GridPane getBoardGrid() {
        return boardGrid;
    }

    /**
     * Clear the current cell selection highlight.
     */
    public void clearSelection() {
        highlightCell(null);
    }

    /**
     * Mark whether animations are in progress (disables input).
     */
    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
        boardGrid.setDisable(animating);
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}
