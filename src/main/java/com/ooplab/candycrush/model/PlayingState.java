package com.ooplab.candycrush.model;

import com.ooplab.candycrush.view.GameView;

/**
 * Playing state: game is active, player can swap candies.
 * Owns its UI side-effect: clears the status text on entry.
 */
public class PlayingState implements GameState {
    private final GameView view;

    public PlayingState(GameView view) {
        this.view = view;
    }

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
        view.setStatusText("");
    }
}
