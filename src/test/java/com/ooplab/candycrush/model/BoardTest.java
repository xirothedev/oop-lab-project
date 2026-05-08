package com.ooplab.candycrush.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class BoardTest {

    private static final CandyType[] SAFE_PATTERN = {
            CandyType.RED,
            CandyType.BLUE,
            CandyType.GREEN,
            CandyType.YELLOW,
            CandyType.PURPLE,
            CandyType.ORANGE
    };

    @Test
    void constructorCompletesQuicklyAndStartsWithoutMatches() {
        Board board = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> new Board());

        assertFalse(board.hasMatches(), "A new board should not start with existing matches.");
    }

    @Test
    void resolveMatches_createsHorizontalBlastStripedCandyFromVerticalFourAtSwapTarget() {
        Board board = configuredBoard();
        configureVerticalFourMatch(board, CandyType.RED, 3);

        Cell swapFrom = board.getCell(3, 2);
        Cell swapTo = board.getCell(3, 3);

        board.swap(swapFrom, swapTo);

        MatchResolution resolution = board.resolveMatches(swapFrom, swapTo, true);
        board.applyMatchResolution(resolution);

        Candy spawned = board.getCell(3, 3).getCandy();
        StripedCandy stripedCandy = assertInstanceOf(StripedCandy.class, spawned);

        assertEquals(CandyType.RED, stripedCandy.getType());
        assertEquals(BlastDirection.ROW, stripedCandy.getBlastDirection());
        assertTrue(board.getCell(0, 3).isEmpty());
        assertTrue(board.getCell(1, 3).isEmpty());
        assertTrue(board.getCell(2, 3).isEmpty());
    }

    @Test
    void resolveMatches_createsVerticalBlastStripedCandyFromHorizontalFourAtSwapTarget() {
        Board board = configuredBoard();
        configureHorizontalFourMatch(board, CandyType.BLUE, 4);

        Cell swapFrom = board.getCell(2, 4);
        Cell swapTo = board.getCell(3, 4);

        board.swap(swapFrom, swapTo);

        MatchResolution resolution = board.resolveMatches(swapFrom, swapTo, true);
        board.applyMatchResolution(resolution);

        Candy spawned = board.getCell(3, 4).getCandy();
        StripedCandy stripedCandy = assertInstanceOf(StripedCandy.class, spawned);

        assertEquals(CandyType.BLUE, stripedCandy.getType());
        assertEquals(BlastDirection.COLUMN, stripedCandy.getBlastDirection());
        assertTrue(board.getCell(3, 1).isEmpty());
        assertTrue(board.getCell(3, 2).isEmpty());
        assertTrue(board.getCell(3, 3).isEmpty());
    }

    @Test
    void resolveMatches_triggersStripedCandyWhenSwappedWithoutNormalMatch() {
        Board board = configuredBoard();
        setCandy(board, 4, 4, new StripedCandy(CandyType.GREEN, BlastDirection.ROW));

        Cell swapFrom = board.getCell(4, 4);
        Cell swapTo = board.getCell(4, 5);

        board.swap(swapFrom, swapTo);

        MatchResolution resolution = board.resolveMatches(swapFrom, swapTo, true);

        for (int col = 0; col < Board.SIZE; col++) {
            assertTrue(resolution.clearedCells().contains(board.getCell(4, col)));
        }

        board.applyMatchResolution(resolution);

        for (int col = 0; col < Board.SIZE; col++) {
            assertTrue(board.getCell(4, col).isEmpty());
        }
    }

    @Test
    void resolveMatches_chainsStripedExplosionsWhenAnotherStripedCandyIsCaughtInBlast() {
        Board board = configuredBoard();
        setCandy(board, 2, 2, new StripedCandy(CandyType.YELLOW, BlastDirection.ROW));
        setCandy(board, 2, 6, new StripedCandy(CandyType.PURPLE, BlastDirection.COLUMN));

        Cell swapFrom = board.getCell(2, 2);
        Cell swapTo = board.getCell(2, 3);

        board.swap(swapFrom, swapTo);

        MatchResolution resolution = board.resolveMatches(swapFrom, swapTo, true);
        Set<Cell> clearedCells = resolution.clearedCells();

        for (int col = 0; col < Board.SIZE; col++) {
            assertTrue(clearedCells.contains(board.getCell(2, col)));
        }
        for (int row = 0; row < Board.SIZE; row++) {
            assertTrue(clearedCells.contains(board.getCell(row, 6)));
        }
    }

    @Test
    void resolveMatches_spawnsStripedCandyAtRunEndpointForCascadeCreatedFourMatch() {
        Board board = configuredBoard();
        setCandy(board, 5, 0, CandyType.RED);
        setCandy(board, 5, 1, CandyType.ORANGE);
        setCandy(board, 5, 2, CandyType.ORANGE);
        setCandy(board, 5, 3, CandyType.ORANGE);
        setCandy(board, 5, 4, CandyType.ORANGE);

        MatchResolution resolution = board.resolveMatches(null, null, false);
        board.applyMatchResolution(resolution);

        Candy spawned = board.getCell(5, 4).getCandy();
        StripedCandy stripedCandy = assertInstanceOf(StripedCandy.class, spawned);

        assertEquals(CandyType.ORANGE, stripedCandy.getType());
        assertEquals(BlastDirection.COLUMN, stripedCandy.getBlastDirection());
        assertTrue(board.getCell(5, 1).isEmpty());
        assertTrue(board.getCell(5, 2).isEmpty());
        assertTrue(board.getCell(5, 3).isEmpty());
    }

    private Board configuredBoard() {
        Board board = new Board();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                CandyType type = SAFE_PATTERN[(row + col) % SAFE_PATTERN.length];
                setCandy(board, row, col, type);
            }
        }
        return board;
    }

    private void configureVerticalFourMatch(Board board, CandyType type, int matchColumn) {
        setCandy(board, 0, matchColumn, type);
        setCandy(board, 1, matchColumn, type);
        setCandy(board, 2, matchColumn, type);
        setCandy(board, 3, matchColumn - 1, type);
        setCandy(board, 3, matchColumn, CandyType.BLUE);
    }

    private void configureHorizontalFourMatch(Board board, CandyType type, int matchColumn) {
        setCandy(board, 3, matchColumn - 3, type);
        setCandy(board, 3, matchColumn - 2, type);
        setCandy(board, 3, matchColumn - 1, type);
        setCandy(board, 2, matchColumn, type);
        setCandy(board, 3, matchColumn, CandyType.GREEN);
    }

    private void setCandy(Board board, int row, int col, CandyType type) {
        setCandy(board, row, col, new NormalCandy(type));
    }

    private void setCandy(Board board, int row, int col, Candy candy) {
        board.getCell(row, col).setCandy(candy);
    }
}
