package com.ooplab.candycrush.model;

import com.ooplab.candycrush.util.CandyFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Core game board: 8x8 grid of cells.
 * Handles initialization, swapping, match removal, gravity, and refill.
 *
 * The candy source is injected as a {@link Supplier} so tests can supply a
 * deterministic sequence instead of the random factory.
 */
public class Board {
    public static final int SIZE = 8;
    private final Cell[][] grid;
    private final MatchFinder matchFinder;
    private final Supplier<Candy> candySupplier;

    public Board() {
        this(CandyFactory::createRandom);
    }

    public Board(Supplier<Candy> candySupplier) {
        this.candySupplier = candySupplier;
        this.grid = new Cell[SIZE][SIZE];
        this.matchFinder = new MatchFinder();
        initializeGrid();
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

    private void randomizeAllCells() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c].setCandy(candySupplier.get());
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
     * Resolve one board state into the cells that should be cleared and the special candies that should spawn.
     */
    public MatchResolution resolveMatches(Cell swapFrom, Cell swapTo, boolean fromPlayerSwap) {
        List<MatchRun> runs = matchFinder.findMatchRuns(grid);
        Set<Cell> clearedCells = new LinkedHashSet<>();
        List<SpecialSpawn> specialSpawns = new ArrayList<>();
        Set<Cell> reservedSpawnCells = new HashSet<>();
        Deque<Cell> pendingStripedTriggers = new ArrayDeque<>();

        if (fromPlayerSwap) {
            enqueueStripedIfPresent(swapFrom, pendingStripedTriggers);
            enqueueStripedIfPresent(swapTo, pendingStripedTriggers);
        }

        Map<Cell, Integer> runMemberships = countRunMemberships(runs);
        for (MatchRun run : runs) {
            clearedCells.addAll(run.getCells());

            SpecialSpawn spawn = createSpecialSpawn(run, swapFrom, swapTo, fromPlayerSwap, runMemberships);
            if (spawn != null && reservedSpawnCells.add(spawn.getCell())) {
                specialSpawns.add(spawn);
            }
        }

        clearedCells.removeAll(reservedSpawnCells);
        enqueueTriggeredStripedMatches(clearedCells, reservedSpawnCells, pendingStripedTriggers);
        expandStripedTriggers(clearedCells, reservedSpawnCells, pendingStripedTriggers);
        clearedCells.removeAll(reservedSpawnCells);

        return new MatchResolution(clearedCells, specialSpawns);
    }

    /**
     * Apply a previously computed resolution without gravity or refill.
     */
    public void applyMatchResolution(MatchResolution resolution) {
        for (Cell cell : resolution.clearedCells()) {
            cell.clear();
        }
        for (SpecialSpawn spawn : resolution.specialSpawns()) {
            spawn.getCell().setCandy(spawn.getCandy());
        }
    }

    /**
     * Apply gravity: candies fall down to fill empty spaces below them.
     * Returns a map of Cell -> rows dropped for animation purposes.
     */
    public Map<Cell, Integer> applyGravity() {
        Map<Cell, Integer> drops = new HashMap<>();
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
                    grid[r][c].setCandy(candySupplier.get());
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
     * Check if there are any matches currently on the board.
     */
    public boolean hasMatches() {
        return matchFinder.hasMatches(grid);
    }

    private Map<Cell, Integer> countRunMemberships(List<MatchRun> runs) {
        Map<Cell, Integer> memberships = new HashMap<>();
        for (MatchRun run : runs) {
            for (Cell cell : run.getCells()) {
                memberships.merge(cell, 1, Integer::sum);
            }
        }
        return memberships;
    }

    private SpecialSpawn createSpecialSpawn(
            MatchRun run,
            Cell swapFrom,
            Cell swapTo,
            boolean fromPlayerSwap,
            Map<Cell, Integer> runMemberships) {

        if (run.length() != 4 || hasRunOverlap(run, runMemberships)) {
            return null;
        }

        Cell spawnCell;
        if (fromPlayerSwap) {
            spawnCell = pickSwapAnchor(run, swapFrom, swapTo);
            if (spawnCell == null) {
                return null;
            }
        } else {
            spawnCell = run.endpoint();
        }

        BlastDirection blastDirection =
                run.getOrientation() == MatchOrientation.VERTICAL ? BlastDirection.ROW : BlastDirection.COLUMN;
        return new SpecialSpawn(spawnCell, new StripedCandy(run.getCandyType(), blastDirection));
    }

    private Cell pickSwapAnchor(MatchRun run, Cell swapFrom, Cell swapTo) {
        if (swapTo != null && run.contains(swapTo)) {
            return swapTo;
        }
        if (swapFrom != null && run.contains(swapFrom)) {
            return swapFrom;
        }
        return null;
    }

    private boolean hasRunOverlap(MatchRun run, Map<Cell, Integer> runMemberships) {
        for (Cell cell : run.getCells()) {
            if (runMemberships.getOrDefault(cell, 0) > 1) {
                return true;
            }
        }
        return false;
    }

    private void enqueueStripedIfPresent(Cell cell, Deque<Cell> pendingStripedTriggers) {
        if (cell != null && cell.getCandy() instanceof StripedCandy) {
            pendingStripedTriggers.add(cell);
        }
    }

    private void enqueueTriggeredStripedMatches(
            Set<Cell> clearedCells,
            Set<Cell> reservedSpawnCells,
            Deque<Cell> pendingStripedTriggers) {
        for (Cell cell : clearedCells) {
            if (!reservedSpawnCells.contains(cell) && cell.getCandy() instanceof StripedCandy) {
                pendingStripedTriggers.add(cell);
            }
        }
    }

    private void expandStripedTriggers(
            Set<Cell> clearedCells,
            Set<Cell> reservedSpawnCells,
            Deque<Cell> pendingStripedTriggers) {
        Set<Cell> processedTriggers = new HashSet<>();

        while (!pendingStripedTriggers.isEmpty()) {
            Cell source = pendingStripedTriggers.removeFirst();
            if (source == null || processedTriggers.contains(source)) {
                continue;
            }
            if (!(source.getCandy() instanceof StripedCandy stripedCandy)) {
                continue;
            }

            processedTriggers.add(source);
            clearedCells.add(source);

            if (stripedCandy.getBlastDirection() == BlastDirection.ROW) {
                for (int col = 0; col < SIZE; col++) {
                    includeBlastCell(source.getRow(), col, clearedCells, reservedSpawnCells, pendingStripedTriggers);
                }
            } else {
                for (int row = 0; row < SIZE; row++) {
                    includeBlastCell(row, source.getCol(), clearedCells, reservedSpawnCells, pendingStripedTriggers);
                }
            }
        }
    }

    private void includeBlastCell(
            int row,
            int col,
            Set<Cell> clearedCells,
            Set<Cell> reservedSpawnCells,
            Deque<Cell> pendingStripedTriggers) {
        Cell target = getCell(row, col);
        if (target == null || reservedSpawnCells.contains(target)) {
            return;
        }

        clearedCells.add(target);
        if (target.getCandy() instanceof StripedCandy) {
            pendingStripedTriggers.add(target);
        }
    }
}
