package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.NormalCandy;

import java.util.List;
import java.util.Random;

public final class RandomCandySupplier implements CandySupplier {
    private final Random random;
    private final List<CandyColor> palette;

    public RandomCandySupplier(LevelDefinition levelDefinition) {
        this.random = new Random(levelDefinition.seed());
        this.palette = levelDefinition.palette().isEmpty()
                ? List.of(CandyColor.values())
                : levelDefinition.palette();
    }

    @Override
    public Candy nextCandy() {
        return new NormalCandy(palette.get(random.nextInt(palette.size())));
    }
}

