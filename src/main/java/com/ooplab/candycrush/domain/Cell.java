package com.ooplab.candycrush.domain;

public final class Cell {
    private final Position position;
    private Candy candy;
    private boolean jelly;

    public Cell(Position position) {
        this.position = position;
    }

    public Position position() {
        return position;
    }

    public Candy candy() {
        return candy;
    }

    public void setCandy(Candy candy) {
        this.candy = candy;
    }

    public boolean jelly() {
        return jelly;
    }

    public void setJelly(boolean jelly) {
        this.jelly = jelly;
    }
}

