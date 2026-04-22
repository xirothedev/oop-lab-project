package com.ooplab.candycrush.ui.animation;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GravityMoveCalculatorTest {

    private final GravityMoveCalculator calculator = new GravityMoveCalculator();

    @Test
    void detectsCandiesThatFallDown() {
        // Use same candy objects in before/after to test ID tracking
        Candy red = new NormalCandy(CandyColor.RED);
        Candy green = new NormalCandy(CandyColor.GREEN);
        Candy yellow = new NormalCandy(CandyColor.YELLOW);
        Candy orange = new NormalCandy(CandyColor.ORANGE);

        Board before = new Board(4, 2);
        before.setCandy(new Position(0, 0), red);
        before.setCandy(new Position(0, 1), green);
        before.setCandy(new Position(3, 0), yellow);
        before.setCandy(new Position(3, 1), orange);

        Board after = new Board(4, 2);
        // After gravity: red at (2,0), yellow at (3,0), green at (2,1), orange at (3,1)
        after.setCandy(new Position(2, 0), red);
        after.setCandy(new Position(3, 0), yellow);
        after.setCandy(new Position(2, 1), green);
        after.setCandy(new Position(3, 1), orange);

        Map<Position, Position> moves = calculator.calculate(before, after);

        // All 4 candies are matched by ID (including stationary ones at bottom)
        assertEquals(4, moves.size());
        // red fell from row 0 to row 2
        assertEquals(new Position(2, 0), moves.get(new Position(0, 0)));
        // green fell from row 0 to row 2
        assertEquals(new Position(2, 1), moves.get(new Position(0, 1)));
        // yellow stayed at row 3
        assertEquals(new Position(3, 0), moves.get(new Position(3, 0)));
        // orange stayed at row 3
        assertEquals(new Position(3, 1), moves.get(new Position(3, 1)));
    }

    @Test
    void tracksCandyByIdNotByColor() {
        Candy red1 = new NormalCandy(CandyColor.RED);
        Candy red2 = new NormalCandy(CandyColor.RED);

        Board before = new Board(4, 1);
        before.setCandy(new Position(0, 0), red1);
        before.setCandy(new Position(2, 0), red2);

        Board after = new Board(4, 1);
        // After gravity: red1 at row 2, red2 at row 3
        after.setCandy(new Position(2, 0), red1);
        after.setCandy(new Position(3, 0), red2);

        Map<Position, Position> moves = calculator.calculate(before, after);

        assertEquals(2, moves.size());
        assertEquals(new Position(2, 0), moves.get(new Position(0, 0))); // red1 fell 2 rows
        assertEquals(new Position(3, 0), moves.get(new Position(2, 0))); // red2 fell 1 row
    }

    @Test
    void returnsEmptyMapWhenBoardIsEmpty() {
        Board before = new Board(3, 1);
        Board after = new Board(3, 1);

        Map<Position, Position> moves = calculator.calculate(before, after);

        assertTrue(moves.isEmpty());
    }

    @Test
    void stationaryCandiesMappedToSamePosition() {
        Candy r = new NormalCandy(CandyColor.RED);
        Candy g = new NormalCandy(CandyColor.GREEN);

        // Candies already at bottom
        Board before = new Board(3, 1);
        before.setCandy(new Position(1, 0), r);
        before.setCandy(new Position(2, 0), g);

        Board after = new Board(3, 1);
        after.setCandy(new Position(1, 0), r);
        after.setCandy(new Position(2, 0), g);

        Map<Position, Position> moves = calculator.calculate(before, after);

        assertEquals(2, moves.size());
        // Both mapped to same position (no movement)
        assertEquals(new Position(1, 0), moves.get(new Position(1, 0)));
        assertEquals(new Position(2, 0), moves.get(new Position(2, 0)));
    }
}
