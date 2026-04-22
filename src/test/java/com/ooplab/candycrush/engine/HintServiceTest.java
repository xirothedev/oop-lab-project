package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HintServiceTest {

    private final MatchDetector matchDetector = new MatchDetector();
    private final MoveValidator validator = new MoveValidator(matchDetector);
    private final HintService hintService = new HintService(validator);

    @Test
    void findsMoveWhenOneExists() {
        // R B R G / G R R B -> swap B(0,1) with R(1,1) creates RRR at row 0
        Board board = board(
                "RBRG",
                "GRRB",
                "YBOP",
                "OPYG"
        );

        assertTrue(hintService.findMove(board).isPresent());
    }

    @Test
    void findsMoveThatCreatesThreeInARow() {
        Board board = board(
                "RBRG",
                "GRRB",
                "YBOP",
                "OPYG"
        );

        Move move = hintService.findMove(board).orElseThrow();

        assertTrue(validator.isValidSwap(board, move));
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
