package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.BoardEvent;
import com.ooplab.candycrush.domain.BoardEventType;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.GameState;
import com.ooplab.candycrush.domain.GameStatus;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.MatchGroup;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.Position;
import com.ooplab.candycrush.domain.ResolutionResult;
import com.ooplab.candycrush.domain.SpecialType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ResolutionEngine {
    private final MoveValidator moveValidator;
    private final MatchDetector matchDetector;
    private final SpecialCandyFactory specialCandyFactory;
    private final GravityService gravityService;
    private final CandySupplier candySupplier;
    private final GoalEvaluator goalEvaluator;

    public ResolutionEngine(
            MoveValidator moveValidator,
            MatchDetector matchDetector,
            SpecialCandyFactory specialCandyFactory,
            GravityService gravityService,
            CandySupplier candySupplier,
            GoalEvaluator goalEvaluator
    ) {
        this.moveValidator = moveValidator;
        this.matchDetector = matchDetector;
        this.specialCandyFactory = specialCandyFactory;
        this.gravityService = gravityService;
        this.candySupplier = candySupplier;
        this.goalEvaluator = goalEvaluator;
    }

    public ResolutionResult applyMove(Board board, Move move, GameState state, LevelDefinition level) {
        List<BoardEvent> events = new ArrayList<>();
        if (!moveValidator.isValidSwap(board, move)) {
            return new ResolutionResult(false, 0, state.movesLeft(), true, events, state.status());
        }

        board.swap(move.first(), move.second());
        state.decrementMoves();
        events.add(new BoardEvent(BoardEventType.SWAP, Map.of("from", move.first(), "to", move.second())));

        int totalScore = 0;

        if (isSpecialCombo(board, move)) {
            totalScore += resolveSpecialCombo(board, move, events);
        }

        int chain = 1;
        while (true) {
            List<MatchGroup> matches = matchDetector.findMatches(board);
            if (matches.isEmpty()) {
                break;
            }

            Set<Position> clearPositions = new HashSet<>();
            Map<Position, Candy> spawnedSpecials = new HashMap<>();
            for (MatchGroup group : matches) {
                clearPositions.addAll(group.positions());
                SpecialCandyFactory.SpawnedSpecial spawned = specialCandyFactory.create(group, move);
                if (spawned != null) {
                    spawnedSpecials.put(spawned.position(), spawned.candy());
                    events.add(new BoardEvent(BoardEventType.SPECIAL_SPAWN, Map.of(
                            "position", spawned.position(),
                            "specialType", spawned.candy().specialType(),
                            "pattern", spawned.pattern()
                    )));
                }
            }

            expandSpecialEffects(board, clearPositions);
            clearPositions.removeAll(spawnedSpecials.keySet());
            totalScore += clear(board, clearPositions, chain, events);

            for (Map.Entry<Position, Candy> entry : spawnedSpecials.entrySet()) {
                board.setCandy(entry.getKey(), entry.getValue());
            }

            gravityService.collapse(board);
            events.add(new BoardEvent(BoardEventType.GRAVITY, Map.of("columns", board.cols())));
            refill(board);
            chain++;
        }

        state.addScore(totalScore);
        events.add(new BoardEvent(BoardEventType.SCORE, Map.of("delta", totalScore, "score", state.score())));
        GameStatus endState = goalEvaluator.evaluate(state, board, level);
        state.setStatus(endState);
        events.add(new BoardEvent(BoardEventType.GOAL_PROGRESS, Map.of("remainingJelly", board.jellyCount(), "movesLeft", state.movesLeft())));
        if (endState == GameStatus.WON || endState == GameStatus.LOST) {
            events.add(new BoardEvent(BoardEventType.GAME_END, Map.of("status", endState)));
        }
        return new ResolutionResult(true, totalScore, state.movesLeft(), matchDetector.findMatches(board).isEmpty(), events, endState);
    }

    private boolean isSpecialCombo(Board board, Move move) {
        Candy first = board.getCandy(move.first());
        Candy second = board.getCandy(move.second());
        return first != null && second != null && (
                (first.isSpecial() && second.isSpecial()) ||
                        first.specialType() == SpecialType.COLOR_BOMB ||
                        second.specialType() == SpecialType.COLOR_BOMB
        );
    }

    private int resolveSpecialCombo(Board board, Move move, List<BoardEvent> events) {
        Candy first = board.getCandy(move.first());
        Candy second = board.getCandy(move.second());
        Set<Position> clearPositions = new HashSet<>();

        if (first.specialType() == SpecialType.COLOR_BOMB || second.specialType() == SpecialType.COLOR_BOMB) {
            CandyColor targetColor = first.specialType() == SpecialType.COLOR_BOMB ? second.color() : first.color();
            for (Position position : board.positions()) {
                Candy candy = board.getCandy(position);
                if (candy != null && (candy.color() == targetColor || position.equals(move.first()) || position.equals(move.second()))) {
                    clearPositions.add(position);
                }
            }
        } else if (first.specialType() == SpecialType.WRAPPED && second.specialType() == SpecialType.WRAPPED) {
            addWrappedArea(clearPositions, move.first(), board);
            addWrappedArea(clearPositions, move.second(), board);
        } else {
            addRowAndColumn(clearPositions, move.first(), board);
            addRowAndColumn(clearPositions, move.second(), board);
        }
        return clear(board, clearPositions, 1, events);
    }

    private void expandSpecialEffects(Board board, Set<Position> clearPositions) {
        boolean expanded;
        do {
            expanded = false;
            Set<Position> additions = new HashSet<>();
            for (Position position : clearPositions) {
                Candy candy = board.getCandy(position);
                if (candy == null || !candy.isSpecial()) {
                    continue;
                }
                int before = additions.size();
                switch (candy.specialType()) {
                    case STRIPED_ROW -> {
                        for (int col = 0; col < board.cols(); col++) {
                            additions.add(new Position(position.row(), col));
                        }
                    }
                    case STRIPED_COLUMN -> {
                        for (int row = 0; row < board.rows(); row++) {
                            additions.add(new Position(row, position.col()));
                        }
                    }
                    case WRAPPED -> addWrappedArea(additions, position, board);
                    case COLOR_BOMB -> {
                        for (Position cell : board.positions()) {
                            additions.add(cell);
                        }
                    }
                    default -> {
                    }
                }
                expanded = expanded || additions.size() > before;
            }
            expanded = clearPositions.addAll(additions) || expanded;
        } while (expanded);
    }

    private int clear(Board board, Set<Position> clearPositions, int chain, List<BoardEvent> events) {
        int cleared = 0;
        for (Position position : clearPositions) {
            if (!board.isInside(position) || board.getCandy(position) == null) {
                continue;
            }
            board.setCandy(position, null);
            if (board.hasJelly(position)) {
                board.clearJelly(position);
            }
            cleared++;
        }
        if (cleared > 0) {
            events.add(new BoardEvent(BoardEventType.CLEAR, Map.of("positions", List.copyOf(clearPositions), "chain", chain)));
        }
        return cleared * 60 * chain;
    }

    private void refill(Board board) {
        int added = 0;
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                Position position = new Position(row, col);
                if (board.getCandy(position) == null) {
                    board.setCandy(position, candySupplier.nextCandy());
                    added++;
                }
            }
        }
    }

    private void addWrappedArea(Set<Position> positions, Position center, Board board) {
        for (int row = center.row() - 1; row <= center.row() + 1; row++) {
            for (int col = center.col() - 1; col <= center.col() + 1; col++) {
                Position position = new Position(row, col);
                if (board.isInside(position)) {
                    positions.add(position);
                }
            }
        }
    }

    private void addRowAndColumn(Set<Position> positions, Position pivot, Board board) {
        for (int col = 0; col < board.cols(); col++) {
            positions.add(new Position(pivot.row(), col));
        }
        for (int row = 0; row < board.rows(); row++) {
            positions.add(new Position(row, pivot.col()));
        }
    }
}
