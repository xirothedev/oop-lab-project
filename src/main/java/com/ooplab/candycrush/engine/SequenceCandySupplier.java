package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.NormalCandy;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public final class SequenceCandySupplier implements CandySupplier {
    private final Deque<CandyColor> colors = new ArrayDeque<>();

    public SequenceCandySupplier(CandyColor... colors) {
        this.colors.addAll(Arrays.asList(colors));
    }

    @Override
    public Candy nextCandy() {
        CandyColor color = colors.isEmpty() ? CandyColor.RED : colors.removeFirst();
        return new NormalCandy(color);
    }
}

