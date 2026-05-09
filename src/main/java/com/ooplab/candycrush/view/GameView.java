package com.ooplab.candycrush.view;

import com.ooplab.candycrush.model.BlastDirection;
import com.ooplab.candycrush.model.Board;
import com.ooplab.candycrush.model.Candy;
import com.ooplab.candycrush.model.Cell;
import com.ooplab.candycrush.model.StripedCandy;
import com.ooplab.candycrush.util.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX view: renders the 8x8 board grid, score, moves, status, and restart button.
 * Uses PNG images for candies and supports a striped overlay for special candies.
 */
public class GameView {
    private static final int CELL_SIZE = 70;
    private static final int GAP = 3;

    private final Stage stage;
    private final StackPane root;
    private final BorderPane mainLayout;
    private final GridPane boardGrid;
    private final Label scoreLabel;
    private final Label movesLabel;
    private final Label statusLabel;
    private final Button restartButton;

    // Map Cell -> StackPane for animation targeting
    private final Map<Cell, StackPane> cellMap = new HashMap<>();

    private boolean isAnimating = false;

    public GameView(Stage stage) {
        this.stage = stage;
        this.mainLayout = new BorderPane();
        this.root = new StackPane(mainLayout);
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
        restartButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        restartButton.setPrefWidth(120);
        restartButton.setPrefHeight(40);
        restartButton.setStyle(
                "-fx-background-color: #ff3b3b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );
        restartButton.setOnMouseEntered(e -> restartButton.setStyle(
                "-fx-background-color: #ff5c5c;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        ));
        restartButton.setOnMouseExited(e -> restartButton.setStyle(
                "-fx-background-color: #ff3b3b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        ));
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

        mainLayout.setTop(infoPanel);
        mainLayout.setCenter(boardGrid);

        // Background image on the StackPane root.
        root.setStyle(
                "-fx-background-image: url('/images/bg.png');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: 50% center;"
        );

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, 600, 680);
        scene.setFill(Color.LAVENDER);
        stage.setTitle("Candy Crush - OOP Project");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
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
            pane.getChildren().add(createCandyVisual(cell.getCandy()));
        }

        return pane;
    }

    private StackPane createCandyVisual(Candy candy) {
        StackPane candyVisual = new StackPane();

        String imagePath = candy.getType().getImagePath();
        var stream = getClass().getResourceAsStream(imagePath);
        if (stream != null) {
            ImageView iv = new ImageView(new Image(stream));
            iv.setFitWidth(CELL_SIZE - 16);
            iv.setFitHeight(CELL_SIZE - 16);
            iv.setPreserveRatio(true);
            candyVisual.getChildren().add(iv);
        } else {
            // Fallback to colored rectangle if image missing.
            Rectangle candyRect = new Rectangle(CELL_SIZE - 16, CELL_SIZE - 16);
            candyRect.setArcWidth(8);
            candyRect.setArcHeight(8);
            candyRect.setFill(candy.getType().getColor());
            candyRect.setStroke(Color.DARKGRAY);
            candyRect.setStrokeWidth(1);
            candyVisual.getChildren().add(candyRect);
        }

        if (candy instanceof StripedCandy stripedCandy) {
            addStripedOverlay(candyVisual, stripedCandy.getBlastDirection());
        }

        return candyVisual;
    }

    private void addStripedOverlay(StackPane candyVisual, BlastDirection blastDirection) {
        double[] offsets = {-10, 0, 10};

        for (double offset : offsets) {
            Rectangle stripe;
            if (blastDirection == BlastDirection.ROW) {
                stripe = new Rectangle(CELL_SIZE - 24, 4);
                stripe.setTranslateY(offset);
            } else {
                stripe = new Rectangle(4, CELL_SIZE - 24);
                stripe.setTranslateX(offset);
            }

            stripe.setFill(Color.rgb(255, 255, 255, 0.82));
            stripe.setArcWidth(4);
            stripe.setArcHeight(4);
            candyVisual.getChildren().add(stripe);
        }
    }

    /**
     * Highlight a selected cell. Pass {@code null} to clear the current highlight.
     */
    public void highlightCell(Cell cell) {
        for (StackPane pane : cellMap.values()) {
            pane.getChildren().removeIf(n ->
                    n instanceof Rectangle r && r.getStroke() == Color.GOLD && r.getStrokeWidth() == 3);
        }

        if (cell == null) {
            return;
        }

        StackPane pane = cellMap.get(cell);
        if (pane == null) {
            return;
        }

        Rectangle highlight = new Rectangle(CELL_SIZE - 4, CELL_SIZE - 4);
        highlight.setArcWidth(10);
        highlight.setArcHeight(10);
        highlight.setFill(Color.TRANSPARENT);
        highlight.setStroke(Color.GOLD);
        highlight.setStrokeWidth(3);
        pane.getChildren().add(highlight);
    }

    /**
     * Show a floating combo label above a given cell — animated scale + rise + fade.
     */
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
     * Set restart button action — also plays a click sound.
     */
    public void setOnRestart(Runnable action) {
        restartButton.setOnAction(e -> {
            SoundManager.playClick();
            action.run();
        });
    }

    /**
     * Get the StackPane for a given cell.
     */
    public StackPane getCellPane(Cell cell) {
        return cellMap.get(cell);
    }

    public Stage getStage() {
        return stage;
    }

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
