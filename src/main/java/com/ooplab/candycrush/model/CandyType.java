package com.ooplab.candycrush.model;

import javafx.scene.paint.Color;

/**
 * Enum representing the 6 candy types, each with a unique display color.
 */
public enum CandyType {
    RED("🔴", Color.RED),
    BLUE("🔵", Color.BLUE),
    GREEN("🟢", Color.GREEN),
    YELLOW("🟡", Color.YELLOW),
    PURPLE("🟣", Color.PURPLE),
    ORANGE("🟠", Color.ORANGE);

    private final String symbol;
    private final Color color;

    CandyType(String symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }
}
