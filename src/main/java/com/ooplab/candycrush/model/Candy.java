package com.ooplab.candycrush.model;

/**
 * Abstract base class for all candy types.
 * Provides common type field; concrete subclasses add specific behavior.
 */
public abstract class Candy {
    private final CandyType type;

    protected Candy(CandyType type) {
        this.type = type;
    }

    public CandyType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.getSymbol();
    }
}
