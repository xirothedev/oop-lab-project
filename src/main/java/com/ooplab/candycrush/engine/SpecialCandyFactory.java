package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.ColorBombCandy;
import com.ooplab.candycrush.domain.MatchGroup;
import com.ooplab.candycrush.domain.MatchPattern;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.Position;
import com.ooplab.candycrush.domain.SpecialCandy;
import com.ooplab.candycrush.domain.SpecialType;

import java.util.Comparator;

public final class SpecialCandyFactory {

    public SpawnedSpecial create(MatchGroup group, Move move) {
        if (group.pattern() == MatchPattern.LINE_THREE) {
            return null;
        }
        Position spawnPosition = chooseSpawnPosition(group, move);
        return switch (group.pattern()) {
            case LINE_FOUR -> new SpawnedSpecial(
                    spawnPosition,
                    new SpecialCandy(group.color(), isVertical(group) ? SpecialType.STRIPED_COLUMN : SpecialType.STRIPED_ROW),
                    group.pattern()
            );
            case LINE_FIVE -> new SpawnedSpecial(spawnPosition, new ColorBombCandy(), group.pattern());
            case T_OR_L -> new SpawnedSpecial(spawnPosition, new SpecialCandy(group.color(), SpecialType.WRAPPED), group.pattern());
            default -> null;
        };
    }

    private Position chooseSpawnPosition(MatchGroup group, Move move) {
        if (group.positions().contains(move.second())) {
            return move.second();
        }
        if (group.positions().contains(move.first())) {
            return move.first();
        }
        return group.positions().stream()
                .sorted(Comparator.comparingInt(Position::row).thenComparingInt(Position::col))
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private boolean isVertical(MatchGroup group) {
        long distinctCols = group.positions().stream().map(Position::col).distinct().count();
        return distinctCols == 1;
    }

    public record SpawnedSpecial(Position position, com.ooplab.candycrush.domain.Candy candy, MatchPattern pattern) {
    }
}

