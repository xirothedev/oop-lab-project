package com.ooplab.candycrush.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CandyIdTest {

    @Test
    void eachNewCandyGetsUniqueId() {
        Candy c1 = new NormalCandy(CandyColor.RED);
        Candy c2 = new NormalCandy(CandyColor.RED);

        assertNotEquals(c1.id(), c2.id());
    }

    @Test
    void specialCandyGetsUniqueId() {
        Candy s1 = new SpecialCandy(CandyColor.GREEN, SpecialType.STRIPED_ROW);
        Candy s2 = new SpecialCandy(CandyColor.GREEN, SpecialType.STRIPED_ROW);

        assertNotEquals(s1.id(), s2.id());
    }

    @Test
    void colorBombGetsUniqueId() {
        ColorBombCandy b1 = new ColorBombCandy();
        ColorBombCandy b2 = new ColorBombCandy();

        assertNotEquals(b1.id(), b2.id());
    }

    @Test
    void boardCopyPreservesCandyIds() {
        Board board = new Board(3, 3);
        Candy candy = new NormalCandy(CandyColor.RED);
        board.setCandy(new Position(1, 1), candy);

        Board copy = board.copy();

        assertEquals(candy.id(), copy.getCandy(new Position(1, 1)).id());
    }

    @Test
    void sameColorDifferentId() {
        Candy red1 = new NormalCandy(CandyColor.RED);
        Candy red2 = new NormalCandy(CandyColor.RED);

        assertEquals(red1.color(), red2.color());
        assertNotEquals(red1.id(), red2.id());
    }
}
