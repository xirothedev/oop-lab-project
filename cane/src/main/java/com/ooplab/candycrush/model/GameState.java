package com.ooplab.candycrush.model;

/**
 * State Pattern interface: defines behavior for each game state.
 * Controls whether player input is accepted and what happens on state transitions.
 */
public interface GameState {
    String getName();

    /**
     * Whether player can make a move in this state.
     */
    boolean canPlay();

    /**
     * Called when entering this state.
     */
    void onEnter();
}
