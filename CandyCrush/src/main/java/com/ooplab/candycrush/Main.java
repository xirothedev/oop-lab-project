package com.ooplab.candycrush;

import com.ooplab.candycrush.controller.GameController;
import com.ooplab.candycrush.model.Board;
import com.ooplab.candycrush.model.ScoreManager;
import com.ooplab.candycrush.util.MusicManager;
import com.ooplab.candycrush.view.GameView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application entry point.
 * Wires together model, view, and controller (MVC pattern).
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        MusicManager.playBackgroundMusic();
        // Create model components
        Board board = new Board();
        ScoreManager scoreManager = new ScoreManager();

        // Create view
        GameView view = new GameView(primaryStage);

        // Create controller — connects model and view
        new GameController(board, scoreManager, view);

        // Show the window
        view.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
