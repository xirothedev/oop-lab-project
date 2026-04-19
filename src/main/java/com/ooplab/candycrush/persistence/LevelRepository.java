package com.ooplab.candycrush.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ooplab.candycrush.domain.LevelDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class LevelRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<LevelDefinition> loadLevels() {
        try (InputStream stream = getClass().getResourceAsStream("/levels/levels.json")) {
            if (stream == null) {
                throw new IllegalStateException("Missing levels resource.");
            }
            return OBJECT_MAPPER.readValue(stream, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load levels.", exception);
        }
    }
}

