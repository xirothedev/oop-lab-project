package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GravityServiceTest {

    private final GravityService gravityService = new GravityService();

    @Test
    void candiesFallToBottomOfColumn() {
        // Column 0: R at row 0, Y at row 3 -> after collapse: R at row 2, Y at row 3
        // Column 1: G at row 0, O at row 3 -> after collapse: G at row 2, O at row 3
        Board board = new Board(4, 2);
        board.setCandy(new Position(0, 0), candy('R'));
        board.setCandy(new Position(3, 0), candy('Y'));
        board.setCandy(new Position(0, 1), candy('G'));
        board.setCandy(new Position(3, 1), candy('O'));

        gravityService.collapse(board);

        assertNull(board.getCandy(new Position(0, 0)));
        assertNull(board.getCandy(new Position(1, 0)));
        assertEquals(CandyColor.RED, board.getCandy(new Position(2, 0)).color());
        assertEquals(CandyColor.YELLOW, board.getCandy(new Position(3, 0)).color());
        assertNull(board.getCandy(new Position(0, 1)));
        assertNull(board.getCandy(new Position(1, 1)));
        assertEquals(CandyColor.GREEN, board.getCandy(new Position(2, 1)).color());
        assertEquals(CandyColor.ORANGE, board.getCandy(new Position(3, 1)).color());
    }

    @Test
    void emptyCellsFillFromTopAfterCollapse() {
        // Column 0: R at row 1, Y at row 3 -> after: R at row 1, Y at row 2, row 0 empty, row 3 empty
        // Actually gravity pulls down: row 3 O stays, row 2 empty -> R at row 1 falls to row 1 (already settled above Y... no)
        // Let's trace: col 0: row 1=R, row 3=Y. writeRow=3. row 3=Y: stays at 3, writeRow=2. row 2=null. row 1=R: moves to row 2, writeRow=1. row 0=null. fill row 1 with null, row 0 with null.
        // Result: row 0=null, row 1=null, row 2=R, row 3=Y
        Board board = new Board(4, 2);
        board.setCandy(new Position(1, 0), candy('R'));
        board.setCandy(new Position(3, 0), candy('Y'));
        board.setCandy(new Position(1, 1), candy('G'));
        board.setCandy(new Position(3, 1), candy('O'));

        gravityService.collapse(board);

        assertNull(board.getCandy(new Position(0, 0)));
        assertNull(board.getCandy(new Position(1, 0)));
        assertEquals(CandyColor.RED, board.getCandy(new Position(2, 0)).color());
        assertEquals(CandyColor.YELLOW, board.getCandy(new Position(3, 0)).color());
    }

    @Test
    void fullyClearedColumnHasOnlyRemainingCandies() {
        Board board = new Board(4, 1);
        board.setCandy(new Position(0, 0), candy('R'));
        board.setCandy(new Position(3, 0), candy('G'));

        gravityService.collapse(board);

        assertNull(board.getCandy(new Position(0, 0)));
        assertNull(board.getCandy(new Position(1, 0)));
        assertEquals(CandyColor.RED, board.getCandy(new Position(2, 0)).color());
        assertEquals(CandyColor.GREEN, board.getCandy(new Position(3, 0)).color());
    }

    @Test
    void alreadySettledBoardUnchanged() {
        Board board = new Board(4, 4);
        // All rows filled, nothing to fall
        board.setCandy(new Position(2, 0), candy('R'));
        board.setCandy(new Position(3, 0), candy('G'));
        board.setCandy(new Position(2, 1), candy('B'));
        board.setCandy(new Position(3, 1), candy('O'));
        board.setCandy(new Position(2, 2), candy('Y'));
        board.setCandy(new Position(3, 2), candy('P'));
        board.setCandy(new Position(2, 3), candy('R'));
        board.setCandy(new Position(3, 3), candy('G'));

        gravityService.collapse(board);

        assertEquals(CandyColor.RED, board.getCandy(new Position(2, 0)).color());
        assertEquals(CandyColor.GREEN, board.getCandy(new Position(3, 0)).color());
    }

    private Candy candy(char value) {
        return new NormalCandy(CandyColor.fromSymbol(value));
    }
}
