package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.GameState;
import com.ooplab.candycrush.domain.GameStatus;
import com.ooplab.candycrush.domain.GoalType;
import com.ooplab.candycrush.domain.LevelDefinition;

public final class GoalEvaluator {

    public GameStatus evaluate(GameState state, Board board, LevelDefinition level) {
        if (level.goalType() == GoalType.TARGET_SCORE && state.score() >= level.targetScore()) {
            return GameStatus.WON;
        }
        if (level.goalType() == GoalType.CLEAR_JELLY && board.jellyCount() == 0) {
            return GameStatus.WON;
        }
        if (state.movesLeft() <= 0) {
            return GameStatus.LOST;
        }
        return state.status();
    }
}

