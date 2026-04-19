package com.ooplab.candycrush.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void persistsAndReloadsTopScores() {
        LeaderboardRepository repository = new LeaderboardRepository(tempDir.resolve("scores.json"));
        repository.addScore("Ada", 1200, "Classic Warmup");
        repository.addScore("Linus", 800, "Jelly Drop");

        assertEquals(2, repository.loadScores().size());
        assertEquals("Ada", repository.loadScores().get(0).playerName());
    }
}
