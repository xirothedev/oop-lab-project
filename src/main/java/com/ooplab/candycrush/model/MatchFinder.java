package com.ooplab.candycrush.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Scans the board for maximal horizontal and vertical runs of matching candy types.
 */
public class MatchFinder {

    /**
     * Find all maximal straight runs of 3+ candies with the same type.
     */
    public List<MatchRun> findMatchRuns(Cell[][] board) {
        List<MatchRun> runs = new ArrayList<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int r = 0; r < rows; r++) {
            int c = 0;
            while (c < cols) {
                if (board[r][c].isEmpty()) {
                    c++;
                    continue;
                }

                CandyType type = board[r][c].getCandy().getType();
                int start = c;
                while (c < cols && isSameType(board[r][c], type)) {
                    c++;
                }

                int length = c - start;
                if (length >= 3) {
                    List<Cell> cells = new ArrayList<>();
                    for (int i = start; i < c; i++) {
                        cells.add(board[r][i]);
                    }
                    runs.add(new MatchRun(MatchOrientation.HORIZONTAL, cells, type));
                }
            }
        }

        for (int c = 0; c < cols; c++) {
            int r = 0;
            while (r < rows) {
                if (board[r][c].isEmpty()) {
                    r++;
                    continue;
                }

                CandyType type = board[r][c].getCandy().getType();
                int start = r;
                while (r < rows && isSameType(board[r][c], type)) {
                    r++;
                }

                int length = r - start;
                if (length >= 3) {
                    List<Cell> cells = new ArrayList<>();
                    for (int i = start; i < r; i++) {
                        cells.add(board[i][c]);
                    }
                    runs.add(new MatchRun(MatchOrientation.VERTICAL, cells, type));
                }
            }
        }

        return runs;
    }

    /**
     * Find all cells that are part of any match.
     */
    public Set<Cell> findMatches(Cell[][] board) {
        Set<Cell> matched = new LinkedHashSet<>();
        for (MatchRun run : findMatchRuns(board)) {
            matched.addAll(run.getCells());
        }
        return matched;
    }

    /**
     * Check if the board has any valid matches.
     */
    public boolean hasMatches(Cell[][] board) {
        return !findMatchRuns(board).isEmpty();
    }

    private boolean isSameType(Cell cell, CandyType type) {
        return !cell.isEmpty() && cell.getCandy().getType() == type;
    }
}
