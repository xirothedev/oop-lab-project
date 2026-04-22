package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.MatchGroup;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardGeneratorTest {

    private final MatchDetector matchDetector = new MatchDetector();
    private final BoardGenerator generator = new BoardGenerator();

    @Test
    void generatedBoardHasNoPreExistingMatches() {
        LevelDefinition level = LevelDefinition.targetScore("Test", 6, 6, 10, 5000, 42L);
        FixedCandySupplier supplier = new FixedCandySupplier();

        Board board = generator.createInitialBoard(level, supplier);

        List<MatchGroup> matches = matchDetector.findMatches(board);
        assertTrue(matches.isEmpty(), "Board should have no pre-existing matches but found: " + matches.size());
    }

    @Test
    void generatedBoardHasCorrectDimensions() {
        LevelDefinition level = LevelDefinition.targetScore("Test", 4, 5, 10, 5000, 1L);
        FixedCandySupplier supplier = new FixedCandySupplier();

        Board board = generator.createInitialBoard(level, supplier);

        assertEquals(4, board.rows());
        assertEquals(5, board.cols());
    }

    @Test
    void generatedBoardPreservesJellyCells() {
        LevelDefinition level = LevelDefinition.clearJelly("Jelly", 3, 3, 10, 1L);
        FixedCandySupplier supplier = new FixedCandySupplier();

        Board board = generator.createInitialBoard(level, supplier);

        assertEquals(level.jellyCells().size(), board.jellyCount());
    }

    @Test
    void generatedBoardIsFullyFilled() {
        LevelDefinition level = LevelDefinition.targetScore("Test", 5, 5, 10, 5000, 99L);
        FixedCandySupplier supplier = new FixedCandySupplier();

        Board board = generator.createInitialBoard(level, supplier);

        int filledCount = 0;
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                if (board.getCandy(new Position(row, col)) != null) {
                    filledCount++;
                }
            }
        }
        assertEquals(board.rows() * board.cols(), filledCount);
    }

    /** Supplies candies in a repeating cycle that avoids accidental matches. */
    private static class FixedCandySupplier implements CandySupplier {
        private static final CandyColor[] CYCLE = {
                CandyColor.RED, CandyColor.GREEN, CandyColor.BLUE,
                CandyColor.YELLOW, CandyColor.ORANGE, CandyColor.PURPLE
        };
        private int index = 0;

        @Override
        public Candy nextCandy() {
            return new NormalCandy(CYCLE[index++ % CYCLE.length]);
        }
    }
}
