package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.domain.LevelDefinition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public final class LevelSelectController extends BaseController {
    @FXML
    private VBox levelCardContainer;
    @FXML
    private Button playButton;

    private LevelDefinition selectedLevel;

    @Override
    protected void onRouterReady() {
    }

    public void loadLevels() {
        levelCardContainer.getChildren().clear();
        List<LevelDefinition> levels = router().context().levelRepository().loadLevels();

        int index = 0;
        for (LevelDefinition level : levels) {
            index++;
            StackPane card = createLevelCard(level, index);
            levelCardContainer.getChildren().add(card);
        }

        if (!levels.isEmpty()) {
            selectLevel(levels.getFirst());
        }
    }

    private StackPane createLevelCard(LevelDefinition level, int number) {
        VBox card = new VBox(6);
        card.getStyleClass().add("level-card");
        card.setUserData(level);

        Label name = new Label(number + ". " + level.name());
        name.getStyleClass().add("level-card-name");

        String goalText = level.goalType().name().replace("_", " ").toLowerCase();
        String detail = level.goalType() == com.ooplab.candycrush.domain.GoalType.TARGET_SCORE
                ? goalText + ": " + level.targetScore()
                : goalText + " (" + level.jellyCells().size() + " cells)";

        Label detailLabel = new Label(detail + " · " + level.moveLimit() + " moves");
        detailLabel.getStyleClass().add("level-card-detail");

        card.getChildren().addAll(name, detailLabel);

        card.setOnMouseClicked(e -> selectLevel(level));

        return new StackPane(card);
    }

    private void selectLevel(LevelDefinition level) {
        selectedLevel = level;
        for (var node : levelCardContainer.getChildren()) {
            if (node instanceof StackPane wrapper && wrapper.getChildren().getFirst() instanceof VBox card) {
                if (card.getUserData() == level) {
                    card.getStyleClass().add("selected");
                } else {
                    card.getStyleClass().remove("selected");
                }
            }
        }
    }

    @FXML
    private void handleStart() {
        if (selectedLevel != null) {
            router().showGame(selectedLevel);
        }
    }

    @FXML
    private void handleBack() {
        router().showHome();
    }
}
