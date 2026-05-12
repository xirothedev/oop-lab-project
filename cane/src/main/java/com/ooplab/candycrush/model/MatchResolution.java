package com.ooplab.candycrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable description of one resolution step on the board.
 */
public class MatchResolution {
    private final Set<Cell> clearedCells;
    private final List<SpecialSpawn> specialSpawns;

    public MatchResolution(Set<Cell> clearedCells, List<SpecialSpawn> specialSpawns) {
        this.clearedCells = Collections.unmodifiableSet(new LinkedHashSet<>(clearedCells));
        this.specialSpawns = Collections.unmodifiableList(new ArrayList<>(specialSpawns));
    }

    public Set<Cell> clearedCells() {
        return clearedCells;
    }

    public List<SpecialSpawn> specialSpawns() {
        return specialSpawns;
    }

    public boolean isEmpty() {
        return clearedCells.isEmpty() && specialSpawns.isEmpty();
    }
}
