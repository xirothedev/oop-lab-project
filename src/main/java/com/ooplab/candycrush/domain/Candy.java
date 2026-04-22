package com.ooplab.candycrush.domain;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Candy {
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);
    private final long id;

    protected Candy() {
        this.id = ID_COUNTER.getAndIncrement();
    }

    protected Candy(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

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

