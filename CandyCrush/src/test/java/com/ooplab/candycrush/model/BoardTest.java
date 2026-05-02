package com.ooplab.candycrush.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class BoardTest {

    @Test
    void constructorCompletesQuicklyAndStartsWithoutMatches() {
        Board board = assertTimeoutPreemptively(Duration.ofSeconds(2), Board::new);

        assertFalse(board.hasMatches(), "A new board should not start with existing matches.");
    }
}
