package com.ooplab.candycrush.model;

import com.ooplab.candycrush.util.CandyFactory;

import java.util.Set;

/**
 * Core game board: 8x8 grid of cells.
 * Handles initialization, swapping, match removal, gravity, and refill.
 */
public class Board {
    public static final int SIZE = 8;
    private final Cell[][] grid;
    private final MatchFinder matchFinder;

    public Board() {
        this.grid = new Cell[SIZE][SIZE];
        this.matchFinder = new MatchFinder();
        initializeGrid();
        // Ensure no initial matches
        while (matchFinder.hasMatches(grid)) {
            randomizeAllCells();
        }
    }

    private void initializeGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
        randomizeAllCells();
    }

    private void fillGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    grid[r][c].setCandy(CandyFactory.createRandom());
                }
            }
        }
    }

    private void randomizeAllCells() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c].setCandy(CandyFactory.createRandom());
            }
        }
    }

    /**
     * Swap candies between two adjacent cells. Caller must validate adjacency.
     */
    public void swap(Cell a, Cell b) {
        Candy temp = a.getCandy();
        a.setCandy(b.getCandy());
        b.setCandy(temp);
    }

    /**
     * Get cell at given position.
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return null;
        }
        return grid[row][col];
    }

    /**
     * Get the full grid (read-only for view layer).
     */
    public Cell[][] getGrid() {
        return grid;
    }

    /**
     * Remove all candies in the matched set, leaving cells empty.
     */
    public int removeMatches(Set<Cell> matched) {
        for (Cell cell : matched) {
            cell.clear();
        }
        return matched.size();
    }

    /**
     * Apply gravity: candies fall down to fill empty spaces below them.
     * Returns a map of Cell -> rows dropped for animation purposes.
     */
    public java.util.Map<Cell, Integer> applyGravity() {
        java.util.Map<Cell, Integer> drops = new java.util.HashMap<>();
        for (int c = 0; c < SIZE; c++) {
            int writeRow = SIZE - 1;
            for (int r = SIZE - 1; r >= 0; r--) {
                if (!grid[r][c].isEmpty()) {
                    if (r != writeRow) {
                        grid[writeRow][c].setCandy(grid[r][c].getCandy());
                        grid[r][c].clear();
                        drops.put(grid[writeRow][c], writeRow - r);
                    }
                    writeRow--;
                }
            }
        }
        return drops;
    }

    /**
     * Fill empty cells (top rows after gravity) with new random candies.
     */
    public void fillEmpty() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    grid[r][c].setCandy(CandyFactory.createRandom());
                }
            }
        }
    }

    /**
     * Reset the board with fresh random candies, ensuring no initial matches.
     */
    public void reset() {
        randomizeAllCells();
        // Remove any accidental initial matches
        while (matchFinder.hasMatches(grid)) {
            randomizeAllCells();
        }
    }

    /**
     * Find current matches on the board.
     */
    public Set<Cell> findMatches() {
        return matchFinder.findMatches(grid);
    }

    /**
     * Check if there are any matches currently on the board.
     */
    public boolean hasMatches() {
        return matchFinder.hasMatches(grid);
    }
}
