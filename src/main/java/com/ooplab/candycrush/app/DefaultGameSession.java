package com.ooplab.candycrush.app;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.GameState;
import com.ooplab.candycrush.domain.GameStatus;
import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.domain.Move;
import com.ooplab.candycrush.domain.ResolutionResult;
import com.ooplab.candycrush.engine.BoardGenerator;

import java.util.Optional;
import com.ooplab.candycrush.engine.CandySupplier;
import com.ooplab.candycrush.engine.GoalEvaluator;
import com.ooplab.candycrush.engine.GravityService;
import com.ooplab.candycrush.engine.MatchDetector;
import com.ooplab.candycrush.engine.MoveValidator;
import com.ooplab.candycrush.engine.HintService;
import com.ooplab.candycrush.engine.RandomCandySupplier;
import com.ooplab.candycrush.engine.ResolutionEngine;
import com.ooplab.candycrush.engine.SpecialCandyFactory;

public final class DefaultGameSession implements GameSession {
    private final MatchDetector matchDetector = new MatchDetector();
    private final MoveValidator moveValidator = new MoveValidator(matchDetector);
    private final GoalEvaluator goalEvaluator = new GoalEvaluator();
    private final BoardGenerator boardGenerator = new BoardGenerator();

    private ResolutionEngine resolutionEngine;
    private HintService hintService;
    private CandySupplier candySupplier;
    private LevelDefinition level;
    private Board board;
    private GameState gameState;

    @Override
    public void start(LevelDefinition levelDefinition) {
        this.level = levelDefinition;
        this.candySupplier = new RandomCandySupplier(levelDefinition);
        this.resolutionEngine = new ResolutionEngine(
                moveValidator,
                matchDetector,
                new SpecialCandyFactory(),
                new GravityService(),
                candySupplier,
                goalEvaluator
        );
        this.hintService = new HintService(moveValidator);
        this.board = boardGenerator.createInitialBoard(levelDefinition, candySupplier);
        this.gameState = new GameState(0, levelDefinition.moveLimit(), GameStatus.RUNNING);
    }

    @Override
    public ResolutionResult applyMove(Move move) {
        return resolutionEngine.applyMove(board, move, gameState, level);
    }

    @Override
    public void restart() {
        start(level);
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public Board getBoardCopy() {
        return board.copy();
    }

    @Override
    public GameState getState() {
        return gameState;
    }

    @Override
    public LevelDefinition getLevel() {
        return level;
    }

    @Override
    public Optional<Move> findHint() {
        return hintService.findMove(board);
    }
}

