package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.persistence.LeaderboardEntry;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class LeaderboardController extends BaseController {
    @FXML
    private TableView<LeaderboardEntry> leaderboardTable;
    @FXML
    private TableColumn<LeaderboardEntry, String> playerColumn;
    @FXML
    private TableColumn<LeaderboardEntry, Integer> scoreColumn;
    @FXML
    private TableColumn<LeaderboardEntry, String> levelColumn;

    @Override
    protected void onRouterReady() {
        playerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().playerName()));
        scoreColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().score()).asObject());
        levelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().levelName()));
        leaderboardTable.setItems(FXCollections.observableArrayList(router().context().leaderboardRepository().loadScores()));
    }

    @FXML
    private void handleBack() {
        router().showHome();
    }
}
