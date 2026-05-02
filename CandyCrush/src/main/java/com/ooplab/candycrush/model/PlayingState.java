package com.ooplab.candycrush.model;

/**
 * Playing state: game is active, player can swap candies.
 */
public class PlayingState implements GameState {

    @Override
    public String getName() {
        return "Playing";
    }

    @Override
    public boolean canPlay() {
        return true;
    }

    @Override
    public void onEnter() {
        // No special setup needed
    }
}
