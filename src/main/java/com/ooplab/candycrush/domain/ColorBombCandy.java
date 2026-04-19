package com.ooplab.candycrush.domain;

public final class ColorBombCandy extends Candy {

    @Override
    public CandyColor color() {
        return null;
    }

    @Override
    public SpecialType specialType() {
        return SpecialType.COLOR_BOMB;
    }

    @Override
    public Candy copy() {
        return new ColorBombCandy();
    }
}

