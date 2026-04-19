package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.domain.LevelDefinition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public final class LevelSelectController extends BaseController {
    @FXML
    private ListView<LevelDefinition> levelList;
    @FXML
    private Label levelSummary;

    @Override
    protected void onRouterReady() {
        levelList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LevelDefinition item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        levelList.getSelectionModel().selectedItemProperty().addListener((obs, oldLevel, newLevel) -> updateSummary(newLevel));
    }

    public void loadLevels() {
        List<LevelDefinition> levels = router().context().levelRepository().loadLevels();
        levelList.setItems(FXCollections.observableArrayList(levels));
        if (!levels.isEmpty()) {
            levelList.getSelectionModel().selectFirst();
        }
    }

    private void updateSummary(LevelDefinition level) {
        if (level == null) {
            levelSummary.setText("Select a level to see its goal.");
            return;
        }
        levelSummary.setText(
                "Goal: " + level.goalType() +
                        "\nMoves: " + level.moveLimit() +
                        "\nTarget score: " + level.targetScore() +
                        "\nJelly cells: " + level.jellyCells().size()
        );
    }

    @FXML
    private void handleStart() {
        LevelDefinition selected = levelList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            router().showGame(selected);
        }
    }

    @FXML
    private void handleBack() {
        router().showHome();
    }
}

