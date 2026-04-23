package com.ooplab.candycrush.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Central animation service for all candy transitions.
 * Each method completes fully before calling its callback.
 *
 * Feel notes:
 *  - Gravity uses EASE_IN (accelerating) + sqrt duration scaling so long falls
 *    do not drag; landing squash-stretch adds weight perception.
 *  - Spawn uses EASE_OUT (decelerating) + fade-in; small settle bounce.
 *  - Removal does a pulse-glow then rotate-shrink-fade.
 *  - Swap is EASE_BOTH for a clean back-and-forth.
 */
public class AnimationManager {

    private static final double CELL_PITCH = 63.0; // CELL_SIZE(60) + GAP(3)

    private static final Duration SWAP_DURATION = Duration.millis(220);
    private static final Duration PULSE_DURATION = Duration.millis(140);
    private static final Duration REMOVAL_DURATION = Duration.millis(320);
    private static final Duration GRAVITY_BASE_PER_ROW = Duration.millis(95);
    private static final Duration COLUMN_STAGGER = Duration.millis(35);
    private static final Duration LAND_SQUASH_DURATION = Duration.millis(90);
    private static final Duration LAND_RECOVER_DURATION = Duration.millis(110);
    private static final Duration SPAWN_DURATION = Duration.millis(360);
    private static final Duration SPAWN_SETTLE_DURATION = Duration.millis(110);

    private static final double LAND_SQUASH_X = 1.14;
    private static final double LAND_SQUASH_Y = 0.82;
    private static final double SPAWN_OVERSHOOT = 1.08;

    /**
     * Animate two cell panes swapping positions.
     */
    public void playSwap(StackPane paneA, StackPane paneB, Runnable callback) {
        int colA = safeCol(paneA);
        int colB = safeCol(paneB);
        int rowA = safeRow(paneA);
        int rowB = safeRow(paneB);

        double deltaX = (colB - colA) * (paneA.getPrefWidth() + 3);
        double deltaY = (rowB - rowA) * (paneA.getPrefHeight() + 3);

        TranslateTransition translateA = new TranslateTransition(SWAP_DURATION, paneA);
        translateA.setToX(deltaX);
        translateA.setToY(deltaY);
        translateA.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition translateB = new TranslateTransition(SWAP_DURATION, paneB);
        translateB.setToX(-deltaX);
        translateB.setToY(-deltaY);
        translateB.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition parallel = new ParallelTransition(translateA, translateB);
        parallel.setOnFinished(e -> {
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
     * Animate removal: glow pulse, then rotate + scale + fade out.
     */
    public void playRemoval(List<StackPane> cellPanes, Runnable callback) {
        if (cellPanes.isEmpty()) {
            callback.run();
            return;
        }

        ParallelTransition all = new ParallelTransition();
        for (StackPane pane : cellPanes) {
            Node candyNode = getCandyNode(pane);
            if (candyNode == null) {
                continue;
            }

            // Pulse: scale up briefly (glow suggestion)
            ScaleTransition pulseUp = new ScaleTransition(PULSE_DURATION, candyNode);
            pulseUp.setToX(1.25);
            pulseUp.setToY(1.25);
            pulseUp.setInterpolator(Interpolator.EASE_OUT);

            // Vanish: rotate + shrink + fade
            ScaleTransition shrink = new ScaleTransition(REMOVAL_DURATION, candyNode);
            shrink.setToX(0);
            shrink.setToY(0);
            shrink.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade = new FadeTransition(REMOVAL_DURATION, candyNode);
            fade.setFromValue(1);
            fade.setToValue(0);

            RotateTransition rotate = new RotateTransition(REMOVAL_DURATION, candyNode);
            rotate.setByAngle(180);
            rotate.setInterpolator(Interpolator.EASE_IN);

            ParallelTransition vanish = new ParallelTransition(shrink, fade, rotate);
            SequentialTransition perCandy = new SequentialTransition(pulseUp, vanish);
            all.getChildren().add(perCandy);
        }

        all.setOnFinished(e -> callback.run());
        all.play();
    }

    /**
     * Gravity: candies fall with accelerating ease + landing squash-stretch.
     * Per-column stagger creates a wave effect instead of uniform drop.
     */
    public void playGravity(Map<StackPane, Integer> dropDistances, Runnable callback) {
        if (dropDistances.isEmpty()) {
            callback.run();
            return;
        }

        ParallelTransition all = new ParallelTransition();

        for (Map.Entry<StackPane, Integer> entry : dropDistances.entrySet()) {
            StackPane pane = entry.getKey();
            int rowsDropped = entry.getValue();
            if (rowsDropped <= 0) {
                continue;
            }

            Duration fallDuration = GRAVITY_BASE_PER_ROW.multiply(Math.sqrt(rowsDropped) + 0.5);

            TranslateTransition fall = new TranslateTransition(fallDuration, pane);
            fall.setFromY(-rowsDropped * CELL_PITCH);
            fall.setToY(0);
            fall.setInterpolator(Interpolator.EASE_IN);

            // Landing squash + recover
            Node candyNode = getCandyNode(pane);
            SequentialTransition perPane;
            if (candyNode != null) {
                ScaleTransition squash = new ScaleTransition(LAND_SQUASH_DURATION, candyNode);
                squash.setToX(LAND_SQUASH_X);
                squash.setToY(LAND_SQUASH_Y);
                squash.setInterpolator(Interpolator.EASE_OUT);

                ScaleTransition recover = new ScaleTransition(LAND_RECOVER_DURATION, candyNode);
                recover.setToX(1.0);
                recover.setToY(1.0);
                recover.setInterpolator(Interpolator.EASE_OUT);

                perPane = new SequentialTransition(fall, squash, recover);
            } else {
                perPane = new SequentialTransition(fall);
            }

            int col = safeCol(pane);
            perPane.setDelay(COLUMN_STAGGER.multiply(col));
            all.getChildren().add(perPane);
        }

        all.setOnFinished(e -> {
            for (StackPane pane : dropDistances.keySet()) {
                pane.setTranslateX(0);
                pane.setTranslateY(0);
                Node candy = getCandyNode(pane);
                if (candy != null) {
                    candy.setScaleX(1.0);
                    candy.setScaleY(1.0);
                }
            }
            callback.run();
        });
        all.play();
    }

    /**
     * New candies slide in from above with ease-out + fade-in + tiny settle bounce.
     */
    public void playSpawn(List<StackPane> spawnPanes, Map<StackPane, Integer> rowMap, Runnable callback) {
        if (spawnPanes.isEmpty()) {
            callback.run();
            return;
        }

        ParallelTransition all = new ParallelTransition();

        for (StackPane pane : spawnPanes) {
            Integer row = rowMap.getOrDefault(pane, 0);
            double startY = -(row + 1) * CELL_PITCH;

            pane.setTranslateY(startY);
            Node candyNode = getCandyNode(pane);
            if (candyNode != null) {
                candyNode.setOpacity(0);
            }

            TranslateTransition drop = new TranslateTransition(SPAWN_DURATION, pane);
            drop.setToY(0);
            drop.setInterpolator(Interpolator.EASE_IN);

            ParallelTransition arrive;
            if (candyNode != null) {
                FadeTransition fadeIn = new FadeTransition(SPAWN_DURATION.divide(2), candyNode);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                arrive = new ParallelTransition(drop, fadeIn);
            } else {
                arrive = new ParallelTransition(drop);
            }

            SequentialTransition perPane;
            if (candyNode != null) {
                ScaleTransition overshoot = new ScaleTransition(SPAWN_SETTLE_DURATION, candyNode);
                overshoot.setToX(SPAWN_OVERSHOOT);
                overshoot.setToY(SPAWN_OVERSHOOT);
                overshoot.setInterpolator(Interpolator.EASE_OUT);

                ScaleTransition settle = new ScaleTransition(SPAWN_SETTLE_DURATION, candyNode);
                settle.setToX(1.0);
                settle.setToY(1.0);
                settle.setInterpolator(Interpolator.EASE_OUT);

                perPane = new SequentialTransition(arrive, overshoot, settle);
            } else {
                perPane = new SequentialTransition(arrive);
            }

            int col = safeCol(pane);
            perPane.setDelay(COLUMN_STAGGER.multiply(col));
            all.getChildren().add(perPane);
        }

        all.setOnFinished(e -> {
            for (StackPane pane : spawnPanes) {
                pane.setTranslateX(0);
                pane.setTranslateY(0);
                Node candy = getCandyNode(pane);
                if (candy != null) {
                    candy.setScaleX(1.0);
                    candy.setScaleY(1.0);
                    candy.setOpacity(1.0);
                }
            }
            callback.run();
        });
        all.play();
    }

    private Node getCandyNode(StackPane pane) {
        if (pane == null || pane.getChildren().size() < 2) {
            return null;
        }
        return pane.getChildren().get(1);
    }

    private int safeCol(Node n) {
        Integer v = GridPane.getColumnIndex(n);
        return v != null ? v : 0;
    }

    private int safeRow(Node n) {
        Integer v = GridPane.getRowIndex(n);
        return v != null ? v : 0;
    }
}
