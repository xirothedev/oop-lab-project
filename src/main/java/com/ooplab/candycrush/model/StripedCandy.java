package com.ooplab.candycrush.model;

/**
 * Special candy that clears an entire row or column when triggered.
 */
public class StripedCandy extends Candy {
    private final BlastDirection blastDirection;

    public StripedCandy(CandyType type, BlastDirection blastDirection) {
        super(type);
        this.blastDirection = blastDirection;
    }

    public BlastDirection getBlastDirection() {
        return blastDirection;
    }
}
