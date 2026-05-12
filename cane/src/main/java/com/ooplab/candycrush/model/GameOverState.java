package com.ooplab.candycrush.model;

import com.ooplab.candycrush.view.GameView;

/**
 * Game Over state: no more moves, player must restart.
 * Owns its UI side-effect: shows the final score banner on entry.
 */
public class GameOverState implements GameState {
    private final GameView view;
    private final ScoreManager scoreManager;

    public GameOverState(GameView view, ScoreManager scoreManager) {
        this.view = view;
        this.scoreManager = scoreManager;
    }

    @Override
    public String getName() {
        return "Game Over";
    }

    @Override
    public boolean canPlay() {
        return false;
    }

    @Override
    public void onEnter() {
        view.setStatusText("Game Over! Final Score: " + scoreManager.getScore());
    }
}
