package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.Position;

import java.util.Optional;

public final class HintService {
    private final MoveValidator validator;

    public HintService(MoveValidator validator) {
        this.validator = validator;
    }

    public Optional<Move> findMove(Board board) {
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position current = new Position(row, col);
                Position right = new Position(row, col + 1);
                Position down = new Position(row + 1, col);
                if (board.isInside(right)) {
                    Move move = new Move(current, right);
                    if (validator.isValidSwap(board, move)) {
                        return Optional.of(move);
                    }
                }
                if (board.isInside(down)) {
                    Move move = new Move(current, down);
                    if (validator.isValidSwap(board, move)) {
                        return Optional.of(move);
                    }
                }
            }
        }
        return Optional.empty();
    }
}

