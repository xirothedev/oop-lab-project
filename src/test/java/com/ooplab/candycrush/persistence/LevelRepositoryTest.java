package com.ooplab.candycrush.persistence;

import com.ooplab.candycrush.domain.GoalType;
import com.ooplab.candycrush.domain.LevelDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelRepositoryTest {

    @Test
    void loadsBundledLevelsFromJsonResource() {
        LevelRepository repository = new LevelRepository();

        List<LevelDefinition> levels = repository.loadLevels();

        assertEquals(3, levels.size());
        assertEquals("Classic Warmup", levels.get(0).name());
        assertEquals(GoalType.TARGET_SCORE, levels.get(0).goalType());
        assertTrue(levels.stream().anyMatch(level -> level.goalType() == GoalType.CLEAR_JELLY));
    }
}

