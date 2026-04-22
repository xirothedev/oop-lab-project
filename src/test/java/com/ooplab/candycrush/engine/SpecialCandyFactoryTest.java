package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpecialCandyFactoryTest {

    private final SpecialCandyFactory factory = new SpecialCandyFactory();

    @Test
    void lineThreeCreatesNoSpecialCandy() {
        MatchGroup group = new MatchGroup(
                Set.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)),
                MatchPattern.LINE_THREE,
                CandyColor.RED
        );
        Move move = new Move(new Position(0, 2), new Position(0, 3));

        assertNull(factory.create(group, move));
    }

    @Test
    void lineFourCreatesStripedCandy() {
        MatchGroup group = new MatchGroup(
                Set.of(new Position(1, 0), new Position(1, 1), new Position(1, 2), new Position(1, 3)),
                MatchPattern.LINE_FOUR,
                CandyColor.GREEN
        );
        Move move = new Move(new Position(1, 2), new Position(1, 3));

        SpecialCandyFactory.SpawnedSpecial result = factory.create(group, move);

        assertNotNull(result);
        assertEquals(SpecialType.STRIPED_ROW, result.candy().specialType());
        assertTrue(move.second().equals(result.position()) || move.first().equals(result.position()));
    }

    @Test
    void verticalLineFourCreatesColumnStriped() {
        MatchGroup group = new MatchGroup(
                Set.of(new Position(0, 2), new Position(1, 2), new Position(2, 2), new Position(3, 2)),
                MatchPattern.LINE_FOUR,
                CandyColor.BLUE
        );
        Move move = new Move(new Position(2, 2), new Position(3, 2));

        SpecialCandyFactory.SpawnedSpecial result = factory.create(group, move);

        assertNotNull(result);
        assertEquals(SpecialType.STRIPED_COLUMN, result.candy().specialType());
    }

    @Test
    void lineFiveCreatesColorBomb() {
        MatchGroup group = new MatchGroup(
                Set.of(new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(0, 3), new Position(0, 4)),
                MatchPattern.LINE_FIVE,
                CandyColor.RED
        );
        Move move = new Move(new Position(0, 3), new Position(0, 4));

        SpecialCandyFactory.SpawnedSpecial result = factory.create(group, move);

        assertNotNull(result);
        assertEquals(SpecialType.COLOR_BOMB, result.candy().specialType());
    }

    @Test
    void tShapeCreatesWrappedCandy() {
        MatchGroup group = new MatchGroup(
                Set.of(
                        new Position(1, 2),
                        new Position(2, 0), new Position(2, 1),
                        new Position(2, 2), new Position(2, 3)
                ),
                MatchPattern.T_OR_L,
                CandyColor.ORANGE
        );
        Move move = new Move(new Position(2, 2), new Position(2, 3));

        SpecialCandyFactory.SpawnedSpecial result = factory.create(group, move);

        assertNotNull(result);
        assertEquals(SpecialType.WRAPPED, result.candy().specialType());
    }

    @Test
    void spawnedCandyPreservesMatchColor() {
        MatchGroup group = new MatchGroup(
                Set.of(new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(0, 3)),
                MatchPattern.LINE_FOUR,
                CandyColor.PURPLE
        );
        Move move = new Move(new Position(0, 2), new Position(0, 3));

        SpecialCandyFactory.SpawnedSpecial result = factory.create(group, move);

        assertEquals(CandyColor.PURPLE, result.candy().color());
    }
}
