package com.ooplab.candycrush.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Central animation service for all candy transitions.
 * Each method completes fully before calling its callback.
 */
public class AnimationManager {

    private static final Duration SWAP_DURATION = Duration.millis(250);
    private static final Duration REMOVAL_DURATION = Duration.millis(350);
    private static final Duration GRAVITY_DURATION_PER_ROW = Duration.millis(120);
    private static final Duration ROW_STAGGER_DELAY = Duration.millis(80);
    private static final Duration SPAWN_DURATION = Duration.millis(350);
    private static final Duration PAUSE_BEFORE_REMOVAL = Duration.millis(100);
    private static final Duration PAUSE_BEFORE_SPAWN = Duration.millis(150);

    /**
     * Animate two cell panes swapping positions.
     * Callback fires only after animation completes.
     */
    public void playSwap(StackPane paneA, StackPane paneB, Runnable callback) {
        Integer colA = GridPane.getColumnIndex(paneA);
        Integer colB = GridPane.getColumnIndex(paneB);
        Integer rowA = GridPane.getRowIndex(paneA);
        Integer rowB = GridPane.getRowIndex(paneB);

        double deltaX = (colB - colA) * (paneA.getPrefWidth() + 3);
        double deltaY = (rowB - rowA) * (paneA.getPrefHeight() + 3);

        TranslateTransition translateA = new TranslateTransition(SWAP_DURATION, paneA);
        translateA.setToX(deltaX);
        translateA.setToY(deltaY);

        TranslateTransition translateB = new TranslateTransition(SWAP_DURATION, paneB);
        translateB.setToX(-deltaX);
        translateB.setToY(-deltaY);

        ParallelTransition parallel = new ParallelTransition(translateA, translateB);
        parallel.setOnFinished(e -> {
            // Reset translate immediately so model position matches visual
            paneA.setTranslateX(0);
            paneA.setTranslateY(0);
            paneB.setTranslateX(0);
            paneB.setTranslateY(0);
            callback.run();
        });
        parallel.play();
    }

    /**
     * Animate swap reversal (invalid move).
     */
    public void playSwapBack(StackPane paneA, StackPane paneB, Runnable callback) {
        playSwap(paneA, paneB, callback);
    }

    /**
     * Animate removal: shrink + fade out candy rectangles.
     * Small initial pause for visual clarity.
     */
    public void playRemoval(List<StackPane> cellPanes, Runnable callback) {
        if (cellPanes.isEmpty()) {
            callback.run();
            return;
        }

        SequentialTransition seq = new SequentialTransition();

        // Brief pause before removal starts
        seq.getChildren().add(new PauseTransition(PAUSE_BEFORE_REMOVAL));

        ParallelTransition allRemovals = new ParallelTransition();
        for (StackPane pane : cellPanes) {
            Node candyNode = getCandyNode(pane);
            if (candyNode != null) {
                ScaleTransition scale = new ScaleTransition(REMOVAL_DURATION, candyNode);
                scale.setToX(0);
                scale.setToY(0);

                FadeTransition fade = new FadeTransition(REMOVAL_DURATION, candyNode);
                fade.setFromValue(1);
                fade.setToValue(0);

                allRemovals.getChildren().add(new ParallelTransition(scale, fade));
            }
        }
        seq.getChildren().add(allRemovals);
        seq.setOnFinished(e -> callback.run());
        seq.play();
    }

    /**
     * Animate gravity: candies fall row-by-row with stagger delay.
     * Bottom candies fall first, upper ones follow (cascade effect).
     */
    public void playGravity(Map<StackPane, Integer> dropDistances, Runnable callback) {
        if (dropDistances.isEmpty()) {
            callback.run();
            return;
        }

        SequentialTransition seq = new SequentialTransition();

        // Sort by row (bottom first = higher row index first)
        List<Map.Entry<StackPane, Integer>> sorted = new ArrayList<>(dropDistances.entrySet());
        sorted.sort(Comparator.<Map.Entry<StackPane, Integer>, Integer>comparing(
                e -> GridPane.getRowIndex(e.getKey()) != null ? GridPane.getRowIndex(e.getKey()) : 0
        ).reversed());

        ParallelTransition allFalls = new ParallelTransition();
        double cellHeight = 63; // CELL_SIZE(60) + GAP(3)

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<StackPane, Integer> entry = sorted.get(i);
            StackPane pane = entry.getKey();
            int rowsDropped = entry.getValue();

            TranslateTransition fall = new TranslateTransition(
                    GRAVITY_DURATION_PER_ROW.multiply(rowsDropped), pane);
            fall.setToY(rowsDropped * cellHeight);

            // Stagger: bottom candies start first, upper ones delayed
            fall.setDelay(ROW_STAGGER_DELAY.multiply(i));

            allFalls.getChildren().add(fall);
        }

        seq.getChildren().add(allFalls);
        seq.setOnFinished(e -> {
            for (StackPane pane : dropDistances.keySet()) {
                pane.setTranslateX(0);
                pane.setTranslateY(0);
            }
            callback.run();
        });
        seq.play();
    }

    /**
     * Animate new candy spawn: slide in from above.
     * Top-row candies spawn first, lower rows stagger later.
     */
    public void playSpawn(List<StackPane> spawnPanes, Map<StackPane, Integer> rowMap, Runnable callback) {
        if (spawnPanes.isEmpty()) {
            callback.run();
            return;
        }

        SequentialTransition seq = new SequentialTransition();

        // Pause before spawn starts (after gravity settles)
        seq.getChildren().add(new PauseTransition(PAUSE_BEFORE_SPAWN));

        ParallelTransition allSpawns = new ParallelTransition();
        for (StackPane pane : spawnPanes) {
            Integer row = rowMap.getOrDefault(pane, 0);
            double startY = -(row + 1) * pane.getPrefHeight();

            pane.setTranslateY(startY);

            TranslateTransition spawn = new TranslateTransition(SPAWN_DURATION, pane);
            spawn.setToY(0);
            // Higher rows (smaller row index) start first
            spawn.setDelay(ROW_STAGGER_DELAY.multiply(row));

            allSpawns.getChildren().add(spawn);
        }

        seq.getChildren().add(allSpawns);
        seq.setOnFinished(e -> {
            for (StackPane pane : spawnPanes) {
                pane.setTranslateX(0);
                pane.setTranslateY(0);
            }
            callback.run();
        });
        seq.play();
    }

    /**
     * Get the candy rectangle node from a cell pane (child index 1).
     */
    private Node getCandyNode(StackPane pane) {
        if (pane.getChildren().size() < 2) {
            return null;
        }
        return pane.getChildren().get(1);
    }
}
