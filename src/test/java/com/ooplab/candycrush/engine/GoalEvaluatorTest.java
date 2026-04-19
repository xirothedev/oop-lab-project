package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.GameState;
import com.ooplab.candycrush.domain.GameStatus;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoalEvaluatorTest {

    private final GoalEvaluator evaluator = new GoalEvaluator();

    @Test
    void marksTargetScoreLevelAsWonWhenScoreReached() {
        Board board = new Board(2, 2);
        fill(board);
        LevelDefinition level = LevelDefinition.targetScore("Score", 2, 2, 5, 100, 1L);
        GameState state = new GameState(120, 3, GameStatus.RUNNING);

        GameStatus status = evaluator.evaluate(state, board, level);

        assertEquals(GameStatus.WON, status);
    }

    @Test
    void marksJellyLevelAsWonWhenAllJellyCleared() {
        Board board = new Board(2, 2);
        fill(board);
        LevelDefinition level = LevelDefinition.clearJelly("Jelly", 2, 2, 5, 1L);
        GameState state = new GameState(10, 2, GameStatus.RUNNING);

        GameStatus status = evaluator.evaluate(state, board, level);

        assertEquals(GameStatus.WON, status);
    }

    private void fill(Board board) {
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                board.setCandy(new Position(row, col), new NormalCandy(CandyColor.RED));
            }
        }
    }
}

