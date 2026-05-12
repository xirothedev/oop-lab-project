package com.ooplab.candycrush.model;

import javafx.scene.paint.Color;

/**
 * Enum representing the 6 candy types, each with a unique display color and image asset.
 */
public enum CandyType {
    RED("🔴", Color.RED, "/images/red.png"),
    BLUE("🔵", Color.BLUE, "/images/blue.png"),
    GREEN("🟢", Color.GREEN, "/images/green.png"),
    YELLOW("🟡", Color.YELLOW, "/images/yellow.png"),
    PURPLE("🟣", Color.PURPLE, "/images/purple.png"),
    ORANGE("🟠", Color.ORANGE, "/images/orange.png");

    private final String symbol;
    private final Color color;
    private final String imagePath;

    CandyType(String symbol, Color color, String imagePath) {
        this.symbol = symbol;
        this.color = color;
        this.imagePath = imagePath;
    }

    public String getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }

    public String getImagePath() {
        return imagePath;
    }
}
