package com.ooplab.candycrush.model;

/**
 * Candy types mapped to PNG images.
 */
public enum CandyType {

    RED("/images/red.png"),
    BLUE("/images/blue.png"),
    GREEN("/images/green.png"),
    YELLOW("/images/yellow.png"),
    PURPLE("/images/purple.png"),
    ORANGE("/images/orange.png");

    private final String imagePath;

    CandyType(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }
}