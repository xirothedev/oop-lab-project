package com.ooplab.candycrush.model;

/**
 * Describes a special candy that should be created after match removal.
 */
public class SpecialSpawn {
    private final Cell cell;
    private final Candy candy;

    public SpecialSpawn(Cell cell, Candy candy) {
        this.cell = cell;
        this.candy = candy;
    }

    public Cell getCell() {
        return cell;
    }

    public Candy getCandy() {
        return candy;
    }
}
