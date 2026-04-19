package com.ooplab.candycrush.domain;

public final class SpecialCandy extends Candy {
    private final CandyColor color;
    private final SpecialType specialType;

    public SpecialCandy(CandyColor color, SpecialType specialType) {
        this.color = color;
        this.specialType = specialType;
    }

    @Override
    public CandyColor color() {
        return color;
    }

    @Override
    public SpecialType specialType() {
        return specialType;
    }

    @Override
    public Candy copy() {
        return new SpecialCandy(color, specialType);
    }
}

