package com.ooplab.candycrush.domain;

import java.util.List;
import java.util.Set;

public record LevelDefinition(
        String name,
        int rows,
        int cols,
        int moveLimit,
        int targetScore,
        GoalType goalType,
        Set<Position> jellyCells,
        long seed,
        List<CandyColor> palette
) {

    public static LevelDefinition targetScore(String name, int rows, int cols, int moveLimit, int targetScore, long seed) {
        return new LevelDefinition(name, rows, cols, moveLimit, targetScore, GoalType.TARGET_SCORE, Set.of(), seed, List.of(CandyColor.values()));
    }

    public static LevelDefinition clearJelly(String name, int rows, int cols, int moveLimit, long seed) {
        return new LevelDefinition(name, rows, cols, moveLimit, 0, GoalType.CLEAR_JELLY, Set.of(), seed, List.of(CandyColor.values()));
    }
}

