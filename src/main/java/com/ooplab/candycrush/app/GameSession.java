package com.ooplab.candycrush.app;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.GameState;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.ResolutionResult;

import java.util.Optional;

public interface GameSession {
    void start(LevelDefinition levelDefinition);

    ResolutionResult applyMove(Move move);

    void restart();

    Board getBoard();

    Board getBoardCopy();

    GameState getState();

    LevelDefinition getLevel();

    Optional<Move> findHint();
}

