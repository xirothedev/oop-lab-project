package com.ooplab.candycrush.app;

import com.ooplab.candycrush.persistence.LeaderboardRepository;
import com.ooplab.candycrush.persistence.LevelRepository;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;

public final class CandyCrushApplication extends Application {

    @Override
    public void start(Stage stage) {
        UiContext context = new UiContext(
                new LevelRepository(),
                new LeaderboardRepository(Path.of(System.getProperty("user.home"), ".ooplab-candy-crush", "leaderboard.json")),
                new DefaultGameSession()
        );
        SceneRouter router = new SceneRouter(stage, context);
        stage.setTitle("Candy Crush OOP Lab");
        router.showHome();
    }
}

