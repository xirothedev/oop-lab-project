package com.ooplab.candycrush.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LeaderboardRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Path filePath;

    public LeaderboardRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<LeaderboardEntry> loadScores() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(filePath.toFile(), new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read leaderboard: " + filePath, exception);
        }
    }

    public void addScore(String playerName, int score, String levelName) {
        List<LeaderboardEntry> entries = new ArrayList<>(loadScores());
        entries.add(new LeaderboardEntry(playerName, score, levelName));
        entries.sort(Comparator.comparingInt(LeaderboardEntry::score).reversed());
        if (entries.size() > 10) {
            entries = new ArrayList<>(entries.subList(0, 10));
        }
        try {
            Files.createDirectories(filePath.getParent());
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), entries);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write leaderboard: " + filePath, exception);
        }
    }
}

