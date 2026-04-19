package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.GameState;
import com.ooplab.candycrush.domain.GameStatus;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.MatchPattern;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import com.ooplab.candycrush.domain.ResolutionResult;
import com.ooplab.candycrush.domain.SpecialType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolutionEngineTest {

    @Test
    void appliesValidMoveCreatesSpecialCandyAndUpdatesScore() {
        Board board = board(
                "RBPG",
                "GRBP",
                "YOBP",
                "OPYP"
        );
        LevelDefinition level = LevelDefinition.targetScore("Demo", 4, 4, 10, 250, 123L);
        GameState state = new GameState(0, 10, GameStatus.RUNNING);
        SequenceCandySupplier supplier = new SequenceCandySupplier(
                CandyColor.YELLOW,
                CandyColor.ORANGE,
                CandyColor.PURPLE,
                CandyColor.GREEN
        );

        ResolutionEngine engine = new ResolutionEngine(
                new MoveValidator(new MatchDetector()),
                new MatchDetector(),
                new SpecialCandyFactory(),
                new GravityService(),
                supplier,
                new GoalEvaluator()
        );

        ResolutionResult result = engine.applyMove(board, new Move(new Position(0, 2), new Position(0, 3)), state, level);

        assertTrue(result.accepted());
        assertTrue(result.scoreDelta() > 0);
        assertEquals(9, state.movesLeft());
        assertEquals(GameStatus.RUNNING, result.endState());
        assertEquals(MatchPattern.LINE_FOUR, result.events().stream()
                .filter(event -> event.type().name().equals("SPECIAL_SPAWN"))
                .findFirst()
                .orElseThrow()
                .payload()
                .get("pattern"));
        assertEquals(SpecialType.STRIPED_COLUMN, board.getCandy(new Position(3, 3)).specialType());
    }

    private Board board(String... rows) {
        Board board = new Board(rows.length, rows[0].length());
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                board.setCandy(new Position(row, col), candy(rows[row].charAt(col)));
            }
        }
        return board;
    }

    private Candy candy(char value) {
        return new NormalCandy(CandyColor.fromSymbol(value));
    }
}
