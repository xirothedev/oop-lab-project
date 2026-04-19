package com.ooplab.candycrush.domain;

public enum CandyColor {
    RED('R'),
    GREEN('G'),
    BLUE('B'),
    YELLOW('Y'),
    ORANGE('O'),
    PURPLE('P');

    private final char symbol;

    CandyColor(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    public static CandyColor fromSymbol(char symbol) {
        char normalized = Character.toUpperCase(symbol);
        for (CandyColor value : values()) {
            if (value.symbol == normalized) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown candy symbol: " + symbol);
    }
}

