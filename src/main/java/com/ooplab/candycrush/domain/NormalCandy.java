package com.ooplab.candycrush.domain;

public final class NormalCandy extends Candy {
    private final CandyColor color;

    public NormalCandy(CandyColor color) {
        this.color = color;
    }

    @Override
    public CandyColor color() {
        return color;
    }

    @Override
    public SpecialType specialType() {
        return SpecialType.NONE;
    }

    @Override
    public Candy copy() {
        return new NormalCandy(color);
    }
}

