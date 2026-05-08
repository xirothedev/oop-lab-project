package com.ooplab.candycrush.util;

import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over candy animations. The controller depends on this interface,
 * not the JavaFX implementation, so tests can swap in a synchronous stub.
 *
 * Every method must invoke {@code callback} exactly once when its animation
 * completes (or immediately if there is nothing to animate).
 */
public interface AnimationManager {

    void playSwap(StackPane paneA, StackPane paneB, Runnable callback);

    void playSwapBack(StackPane paneA, StackPane paneB, Runnable callback);

    void playRemoval(List<StackPane> cellPanes, Runnable callback);

    void playGravity(Map<StackPane, Integer> dropDistances, Runnable callback);

    void playSpawn(List<StackPane> spawnPanes, Map<StackPane, Integer> rowMap, Runnable callback);
}
