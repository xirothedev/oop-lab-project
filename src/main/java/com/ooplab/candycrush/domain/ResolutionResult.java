package com.ooplab.candycrush.domain;

import java.util.List;

public record ResolutionResult(
        boolean accepted,
        int scoreDelta,
        int remainingMoves,
        boolean boardStable,
        List<BoardEvent> events,
        GameStatus endState
) {
}
