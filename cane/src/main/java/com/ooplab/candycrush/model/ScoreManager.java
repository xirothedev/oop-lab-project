package com.ooplab.candycrush.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Manages score and remaining moves.
 * Uses JavaFX Properties for Observer Pattern — UI binds to these and auto-updates.
 */
public class ScoreManager {
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty moves = new SimpleIntegerProperty(20);

    public ScoreManager() {
    }

    /**
     * Add points based on number of matched candies.
     * Each matched candy = 10 points, combo bonus for >3.
     */
    public void addScore(int matchCount) {
        int points = matchCount * 10;
        if (matchCount > 3) {
            points += (matchCount - 3) * 5; // bonus for 4+ matches
        }
        score.set(score.get() + points);
    }

    /**
     * Decrement remaining moves by 1.
     */
    public void useMove() {
        moves.set(moves.get() - 1);
    }

    /**
     * Check if no moves remain.
     */
    public boolean isGameOver() {
        return moves.get() <= 0;
    }

    /**
     * Reset to initial state.
     */
    public void reset() {
        score.set(0);
        moves.set(20);
    }

    // Observable properties for UI binding
    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty movesProperty() {
        return moves;
    }

    public int getScore() {
        return score.get();
    }

    public int getMoves() {
        return moves.get();
    }
}
