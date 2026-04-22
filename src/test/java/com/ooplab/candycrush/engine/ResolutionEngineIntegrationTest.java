package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResolutionEngineIntegrationTest {

    @Test
    void rejectsInvalidMove() {
        Board board = board("RGB", "BYO", "OPY");
        LevelDefinition level = LevelDefinition.targetScore("Test", 3, 3, 10, 5000, 1L);
        GameState state = new GameState(0, 10, GameStatus.RUNNING);

        ResolutionResult result = engine().applyMove(board, new Move(new Position(0, 0), new Position(2, 2)), state, level);

        assertFalse(result.accepted());
        assertEquals(10, state.movesLeft()); // rejected moves don't cost moves
    }

    @Test
    void fourInARowCreatesStripedSpecial() {
        // Row 0: R R R B, col 3 below has R at (1,3)
        // Swap B(0,3) with R(1,3) -> row 0 = R R R R = 4-match
        Board board = board(
                "RRRB",
                "GBYR",
                "OPYG",
                "YBOP"
        );
        LevelDefinition level = LevelDefinition.targetScore("Test", 4, 4, 10, 5000, 1L);
        GameState state = new GameState(0, 10, GameStatus.RUNNING);
        SequenceCandySupplier supplier = new SequenceCandySupplier(
                CandyColor.RED,
                CandyColor.GREEN, CandyColor.BLUE, CandyColor.YELLOW
        );

        ResolutionEngine eng = new ResolutionEngine(
                new MoveValidator(new MatchDetector()),
                new MatchDetector(),
                new SpecialCandyFactory(),
                new GravityService(),
                supplier,
                new GoalEvaluator()
        );

        // Swap (0,3) B with (1,3) R -> row 0 = R R R R = 4-match
        ResolutionResult result = eng.applyMove(board, new Move(new Position(0, 3), new Position(1, 3)), state, level);

        assertTrue(result.accepted());
        boolean hasSpecialSpawn = result.events().stream()
                .anyMatch(e -> e.type() == BoardEventType.SPECIAL_SPAWN);
        assertTrue(hasSpecialSpawn, "Should spawn a striped candy for 4-match");
    }

    @Test
    void colorBombSwapClearsAllSameColor() {
        Board board = board(
                "RGRB",
                "GBYO",
                "OPYR",
                "YBOP"
        );
        board.setCandy(new Position(2, 2), new ColorBombCandy());
        LevelDefinition level = LevelDefinition.targetScore("Test", 4, 4, 10, 5000, 1L);
        GameState state = new GameState(0, 10, GameStatus.RUNNING);
        SequenceCandySupplier supplier = new SequenceCandySupplier(
                CandyColor.YELLOW, CandyColor.ORANGE,
                CandyColor.PURPLE, CandyColor.GREEN
        );

        ResolutionEngine eng = new ResolutionEngine(
                new MoveValidator(new MatchDetector()),
                new MatchDetector(),
                new SpecialCandyFactory(),
                new GravityService(),
                supplier,
                new GoalEvaluator()
        );

        // Swap color bomb (2,2) with adjacent R (2,3) -> clears all RED + both swapped cells
        ResolutionResult result = eng.applyMove(board, new Move(new Position(2, 2), new Position(2, 3)), state, level);

        assertTrue(result.accepted());
        assertTrue(result.scoreDelta() > 0);
    }

    private ResolutionEngine engine() {
        return new ResolutionEngine(
                new MoveValidator(new MatchDetector()),
                new MatchDetector(),
                new SpecialCandyFactory(),
                new GravityService(),
                new SequenceCandySupplier(CandyColor.RED, CandyColor.GREEN, CandyColor.BLUE, CandyColor.YELLOW),
                new GoalEvaluator()
        );
    }

    private Board board(String... rows) {
        Board b = new Board(rows.length, rows[0].length());
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                b.setCandy(new Position(row, col), candy(rows[row].charAt(col)));
            }
        }
        return b;
    }

    private Candy candy(char value) {
        return new NormalCandy(CandyColor.fromSymbol(value));
    }
}
