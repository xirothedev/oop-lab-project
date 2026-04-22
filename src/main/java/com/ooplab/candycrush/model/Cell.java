package com.ooplab.candycrush.model;

/**
 * Represents a single cell on the game board.
 * Holds a reference to a Candy or null if empty.
 */
public class Cell {
    private final int row;
    private final int col;
    private Candy candy;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.candy = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Candy getCandy() {
        return candy;
    }

    public void setCandy(Candy candy) {
        this.candy = candy;
    }

    public boolean isEmpty() {
        return candy == null;
    }

    public void clear() {
        this.candy = null;
    }
}
