package com.ooplab.candycrush.domain;

public final class GameState {
    private int score;
    private int movesLeft;
    private GameStatus status;

    public GameState(int score, int movesLeft, GameStatus status) {
        this.score = score;
        this.movesLeft = movesLeft;
        this.status = status;
    }

    public int score() {
        return score;
    }

    public int movesLeft() {
        return movesLeft;
    }

    public GameStatus status() {
        return status;
    }

    public void addScore(int delta) {
        score += delta;
    }

    public void decrementMoves() {
        movesLeft = Math.max(0, movesLeft - 1);
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}

