package com.ooplab.candycrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one maximal straight match run on the board.
 */
public class MatchRun {
    private final MatchOrientation orientation;
    private final List<Cell> cells;
    private final CandyType candyType;

    public MatchRun(MatchOrientation orientation, List<Cell> cells, CandyType candyType) {
        this.orientation = orientation;
        this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
        this.candyType = candyType;
    }

    public MatchOrientation getOrientation() {
        return orientation;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public CandyType getCandyType() {
        return candyType;
    }

    public int length() {
        return cells.size();
    }

    public boolean contains(Cell cell) {
        return cells.contains(cell);
    }

    public Cell endpoint() {
        return cells.get(cells.size() - 1);
    }
}
