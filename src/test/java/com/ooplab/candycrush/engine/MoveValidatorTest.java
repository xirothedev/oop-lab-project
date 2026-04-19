package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveValidatorTest {

    private final MatchDetector matchDetector = new MatchDetector();
    private final MoveValidator validator = new MoveValidator(matchDetector);

    @Test
    void rejectsSwapWhenCellsAreNotAdjacent() {
        Board board = board(
                "RBG",
                "GBR",
                "YOP"
        );

        boolean valid = validator.isValidSwap(board, new Move(new Position(0, 0), new Position(2, 2)));

        assertFalse(valid);
    }

    @Test
    void rejectsAdjacentSwapThatDoesNotCreateAMatch() {
        Board board = board(
                "RGB",
                "BYO",
                "OPY"
        );

        boolean valid = validator.isValidSwap(board, new Move(new Position(0, 0), new Position(0, 1)));

        assertFalse(valid);
    }

    @Test
    void acceptsAdjacentSwapThatCreatesAMatch() {
        Board board = board(
                "RBG",
                "GRB",
                "YRB"
        );

        boolean valid = validator.isValidSwap(board, new Move(new Position(0, 1), new Position(0, 2)));

        assertTrue(valid);
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
