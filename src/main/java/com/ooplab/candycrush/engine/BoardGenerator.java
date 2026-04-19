package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.Position;

public final class BoardGenerator {
    private final MatchDetector matchDetector;

    public BoardGenerator(MatchDetector matchDetector) {
        this.matchDetector = matchDetector;
    }

    public Board createInitialBoard(LevelDefinition levelDefinition, CandySupplier supplier) {
        Board board = new Board(levelDefinition.rows(), levelDefinition.cols());
        for (Position jelly : levelDefinition.jellyCells()) {
            board.setJelly(jelly, true);
        }
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position position = new Position(row, col);
                do {
                    board.setCandy(position, supplier.nextCandy());
                } while (!matchDetector.findMatches(board.copy()).isEmpty() && hasLocalMatch(board, position));
            }
        }
        return board;
    }

    private boolean hasLocalMatch(Board board, Position position) {
        int row = position.row();
        int col = position.col();
        if (col >= 2 &&
                board.getCandy(new Position(row, col)) != null &&
                board.getCandy(new Position(row, col - 1)) != null &&
                board.getCandy(new Position(row, col - 2)) != null &&
                board.getCandy(new Position(row, col)).matchesColor(board.getCandy(new Position(row, col - 1))) &&
                board.getCandy(new Position(row, col)).matchesColor(board.getCandy(new Position(row, col - 2)))) {
            return true;
        }
        return row >= 2 &&
                board.getCandy(new Position(row, col)) != null &&
                board.getCandy(new Position(row - 1, col)) != null &&
                board.getCandy(new Position(row - 2, col)) != null &&
                board.getCandy(new Position(row, col)).matchesColor(board.getCandy(new Position(row - 1, col))) &&
                board.getCandy(new Position(row, col)).matchesColor(board.getCandy(new Position(row - 2, col)));
    }
}

