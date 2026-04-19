package com.ooplab.candycrush.app;

import com.ooplab.candycrush.domain.LevelDefinition;
import com.ooplab.candycrush.ui.BaseController;
import com.ooplab.candycrush.ui.GameController;
import com.ooplab.candycrush.ui.LevelSelectController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneRouter {
    private final Stage stage;
    private final UiContext context;

    public SceneRouter(Stage stage, UiContext context) {
        this.stage = stage;
        this.context = context;
    }

    public UiContext context() {
        return context;
    }

    public void showHome() {
        show("/fxml/home.fxml");
    }

    public void showLevelSelect() {
        show("/fxml/level-select.fxml");
    }

    public void showLeaderboard() {
        show("/fxml/leaderboard.fxml");
    }

    public void showHelp() {
        show("/fxml/help.fxml");
    }

    public void showGame(LevelDefinition level) {
        show("/fxml/game.fxml", controller -> ((GameController) controller).setLevel(level));
    }

    private void show(String resourcePath) {
        show(resourcePath, controller -> {
        });
    }

    private void show(String resourcePath, ControllerConfigurer configurer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof BaseController baseController) {
                baseController.setRouter(this);
                if (controller instanceof LevelSelectController levelSelectController) {
                    levelSelectController.loadLevels();
                }
            }
            configurer.configure(controller);
            Scene scene = new Scene(root);
            String stylesheet = getClass().getResource("/css/app.css").toExternalForm();
            if (!scene.getStylesheets().contains(stylesheet)) {
                scene.getStylesheets().add(stylesheet);
            }
            stage.setScene(scene);
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load scene " + resourcePath, exception);
        }
    }

    @FunctionalInterface
    private interface ControllerConfigurer {
        void configure(Object controller);
    }
}
