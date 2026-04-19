package com.ooplab.candycrush.app;

import com.ooplab.candycrush.persistence.LeaderboardRepository;
import com.ooplab.candycrush.persistence.LevelRepository;

public record UiContext(LevelRepository levelRepository, LeaderboardRepository leaderboardRepository, GameSession gameSession) {
}

