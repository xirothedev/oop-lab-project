package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.Position;
import com.ooplab.candycrush.domain.SpecialType;

public final class MoveValidator {
    private final MatchDetector matchDetector;

    public MoveValidator(MatchDetector matchDetector) {
        this.matchDetector = matchDetector;
    }

    public boolean isValidSwap(Board board, Move move) {
        Position first = move.first();
        Position second = move.second();
        if (!board.isInside(first) || !board.isInside(second)) {
            return false;
        }
        int distance = Math.abs(first.row() - second.row()) + Math.abs(first.col() - second.col());
        if (distance != 1) {
            return false;
        }

        Candy firstCandy = board.getCandy(first);
        Candy secondCandy = board.getCandy(second);
        if (firstCandy == null || secondCandy == null) {
            return false;
        }
        if (firstCandy.isSpecial() && secondCandy.isSpecial()) {
            return true;
        }
        if (firstCandy.specialType() == SpecialType.COLOR_BOMB || secondCandy.specialType() == SpecialType.COLOR_BOMB) {
            return true;
        }

        Board copy = board.copy();
        copy.swap(first, second);
        return !matchDetector.findMatches(copy).isEmpty();
    }
}
