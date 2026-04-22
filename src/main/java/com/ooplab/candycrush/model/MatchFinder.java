package com.ooplab.candycrush.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Scans the board for horizontal and vertical matches of 3+ consecutive same-type candies.
 * Returns matched cell positions as a Set to avoid duplicates from overlapping matches.
 */
public class MatchFinder {

    /**
     * Find all cells that are part of a match (3+ consecutive same-type in a row or column).
     */
    public Set<Cell> findMatches(Cell[][] board) {
        Set<Cell> matched = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        // Scan rows
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 2; c++) {
                Cell c1 = board[r][c];
                Cell c2 = board[r][c + 1];
                Cell c3 = board[r][c + 2];

                if (!c1.isEmpty() && !c2.isEmpty() && !c3.isEmpty()
                        && c1.getCandy().getType() == c2.getCandy().getType()
                        && c2.getCandy().getType() == c3.getCandy().getType()) {
                    matched.add(c1);
                    matched.add(c2);
                    matched.add(c3);
                }
            }
        }

        // Scan columns
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 2; r++) {
                Cell c1 = board[r][c];
                Cell c2 = board[r + 1][c];
                Cell c3 = board[r + 2][c];

                if (!c1.isEmpty() && !c2.isEmpty() && !c3.isEmpty()
                        && c1.getCandy().getType() == c2.getCandy().getType()
                        && c2.getCandy().getType() == c3.getCandy().getType()) {
                    matched.add(c1);
                    matched.add(c2);
                    matched.add(c3);
                }
            }
        }

        return matched;
    }

    /**
     * Check if the board has any valid matches.
     */
    public boolean hasMatches(Cell[][] board) {
        return !findMatches(board).isEmpty();
    }
}
