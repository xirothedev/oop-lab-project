package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.MatchGroup;
import com.ooplab.candycrush.domain.MatchPattern;
import com.ooplab.candycrush.domain.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MatchDetector {

    public List<MatchGroup> findMatches(Board board) {
        Map<Position, CandyColor> matched = new HashMap<>();
        markHorizontalMatches(board, matched);
        markVerticalMatches(board, matched);
        return groupMatches(matched);
    }

    private void markHorizontalMatches(Board board, Map<Position, CandyColor> matched) {
        for (int row = 0; row < board.rows(); row++) {
            int start = 0;
            while (start < board.cols()) {
                Candy candy = board.getCandy(new Position(row, start));
                if (candy == null || candy.color() == null) {
                    start++;
                    continue;
                }
                int end = start + 1;
                while (end < board.cols()) {
                    Candy next = board.getCandy(new Position(row, end));
                    if (next == null || next.color() != candy.color()) {
                        break;
                    }
                    end++;
                }
                if (end - start >= 3) {
                    for (int col = start; col < end; col++) {
                        matched.put(new Position(row, col), candy.color());
                    }
                }
                start = end;
            }
        }
    }

    private void markVerticalMatches(Board board, Map<Position, CandyColor> matched) {
        for (int col = 0; col < board.cols(); col++) {
            int start = 0;
            while (start < board.rows()) {
                Candy candy = board.getCandy(new Position(start, col));
                if (candy == null || candy.color() == null) {
                    start++;
                    continue;
                }
                int end = start + 1;
                while (end < board.rows()) {
                    Candy next = board.getCandy(new Position(end, col));
                    if (next == null || next.color() != candy.color()) {
                        break;
                    }
                    end++;
                }
                if (end - start >= 3) {
                    for (int row = start; row < end; row++) {
                        matched.put(new Position(row, col), candy.color());
                    }
                }
                start = end;
            }
        }
    }

    private List<MatchGroup> groupMatches(Map<Position, CandyColor> matched) {
        List<MatchGroup> groups = new ArrayList<>();
        Set<Position> visited = new HashSet<>();
        for (Map.Entry<Position, CandyColor> entry : matched.entrySet()) {
            if (visited.contains(entry.getKey())) {
                continue;
            }
            Set<Position> group = new HashSet<>();
            ArrayDeque<Position> queue = new ArrayDeque<>();
            queue.add(entry.getKey());
            visited.add(entry.getKey());
            while (!queue.isEmpty()) {
                Position current = queue.removeFirst();
                group.add(current);
                for (Position neighbor : neighbors(current)) {
                    if (matched.get(neighbor) == entry.getValue() && visited.add(neighbor)) {
                        queue.addLast(neighbor);
                    }
                }
            }
            groups.add(new MatchGroup(group, patternOf(group), entry.getValue()));
        }
        return groups;
    }

    private MatchPattern patternOf(Set<Position> positions) {
        Map<Integer, Integer> rowCounts = new HashMap<>();
        Map<Integer, Integer> colCounts = new HashMap<>();
        for (Position position : positions) {
            rowCounts.merge(position.row(), 1, Integer::sum);
            colCounts.merge(position.col(), 1, Integer::sum);
        }
        int maxRow = rowCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int maxCol = colCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        boolean cross = maxRow >= 3 && maxCol >= 3 && positions.size() >= 5;
        if (cross) {
            return MatchPattern.T_OR_L;
        }
        int longest = Math.max(maxRow, maxCol);
        if (longest >= 5) {
            return MatchPattern.LINE_FIVE;
        }
        if (longest == 4) {
            return MatchPattern.LINE_FOUR;
        }
        return MatchPattern.LINE_THREE;
    }

    private List<Position> neighbors(Position current) {
        return List.of(
                new Position(current.row() - 1, current.col()),
                new Position(current.row() + 1, current.col()),
                new Position(current.row(), current.col() - 1),
                new Position(current.row(), current.col() + 1)
        );
    }
}

