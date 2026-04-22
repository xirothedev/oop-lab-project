package com.ooplab.candycrush.util;

import com.ooplab.candycrush.model.Candy;
import com.ooplab.candycrush.model.CandyType;
import com.ooplab.candycrush.model.NormalCandy;

import java.util.Random;

/**
 * Factory Pattern: creates Candy instances.
 * Centralizes candy creation so the board doesn't need to know concrete types.
 */
public class CandyFactory {
    private static final Random random = new Random();

    /**
     * Create a candy of the specified type.
     */
    public static Candy create(CandyType type) {
        return new NormalCandy(type);
    }

    /**
     * Create a candy with a random type.
     */
    public static Candy createRandom() {
        CandyType[] types = CandyType.values();
        return create(types[random.nextInt(types.length)]);
    }
}
