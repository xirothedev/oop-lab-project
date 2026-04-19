package com.ooplab.candycrush.domain;

public abstract class Candy {

    public abstract CandyColor color();

    public abstract SpecialType specialType();

    public abstract Candy copy();

    public boolean isSpecial() {
        return specialType() != SpecialType.NONE;
    }

    public boolean matchesColor(Candy other) {
        return color() != null && other != null && other.color() == color();
    }
}

