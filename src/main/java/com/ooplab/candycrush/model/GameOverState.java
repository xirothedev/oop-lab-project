package com.ooplab.candycrush.model;

/**
 * Game Over state: no more moves, player must restart.
 */
public class GameOverState implements GameState {

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
        // Triggered when moves reach 0
    }
}
